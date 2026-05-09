package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.model.TradeIntent;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.TradeIntentRepository;
import com.petshop.support.ContentSafety;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/trade-intents")
public class TradeIntentController {
    private static final String STATUS_PENDING = "待处理";
    private static final String STATUS_ACCEPTED = "已同意";
    private static final String STATUS_REJECTED = "已拒绝";
    private static final String STATUS_CANCELED = "已取消";

    private final TradeIntentRepository intents;
    private final MarketPostRepository posts;

    public TradeIntentController(TradeIntentRepository intents, MarketPostRepository posts) {
        this.intents = intents;
        this.posts = posts;
    }

    @GetMapping
    public List<TradeIntent> list(@RequestParam String user, @RequestParam(defaultValue = "requester") String role) {
        requireUser(user);
        List<TradeIntent> items = "owner".equals(role)
                ? intents.findByOwnerOrderByUpdatedAtDesc(user)
                : intents.findByRequesterOrderByUpdatedAtDesc(user);
        items.forEach(this::hydratePost);
        return items;
    }

    @PostMapping
    public TradeIntent create(@RequestBody TradeIntent request) {
        if (request == null || request.getPostId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择要预约的交易帖");
        }
        requireUser(request.getRequester());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));
        if (isBlank(post.getAuthor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "帖子缺少发布者，无法提交意向");
        }
        if (post.getAuthor().equals(request.getRequester())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能给自己的帖子提交意向");
        }
        if (intents.existsByPostIdAndRequester(post.getId(), request.getRequester())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "你已经提交过该帖子意向");
        }
        String message = safe(request.getMessage()).trim();
        if (isBlank(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入意向说明");
        }
        ContentSafety.validate(message);

        LocalDateTime now = LocalDateTime.now();
        TradeIntent intent = new TradeIntent();
        intent.setPostId(post.getId());
        intent.setPostTitle(post.getTitle());
        intent.setRequester(request.getRequester());
        intent.setOwner(post.getAuthor());
        intent.setMessage(message);
        intent.setStatus(STATUS_PENDING);
        intent.setCreatedAt(now);
        intent.setUpdatedAt(now);
        return hydratePost(intents.save(intent));
    }

    @PutMapping("/{id}/status")
    public TradeIntent updateStatus(@PathVariable Long id, @RequestParam String user, @RequestParam String status) {
        requireUser(user);
        TradeIntent intent = intents.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "意向单不存在"));
        if (!user.equals(intent.getOwner()) && !user.equals(intent.getRequester())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能处理自己的意向单");
        }
        if (user.equals(intent.getRequester()) && !STATUS_CANCELED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "买家只能取消自己的意向单");
        }
        if (user.equals(intent.getOwner()) && STATUS_CANCELED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "发布者不能替买家取消意向单");
        }
        if (!STATUS_ACCEPTED.equals(status) && !STATUS_REJECTED.equals(status) && !STATUS_CANCELED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的意向状态");
        }
        intent.setStatus(status);
        intent.setUpdatedAt(LocalDateTime.now());
        return hydratePost(intents.save(intent));
    }

    private TradeIntent hydratePost(TradeIntent intent) {
        if (intent.getPostId() != null) {
            posts.findById(intent.getPostId()).ifPresent(intent::setPost);
        }
        return intent;
    }

    private void requireUser(String user) {
        if (isBlank(user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再使用交易意向");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
