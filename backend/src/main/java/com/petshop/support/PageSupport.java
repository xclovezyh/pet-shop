package com.petshop.support;

import com.petshop.dto.common.PageResponse;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PageSupport {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private PageSupport() {
    }

    public static int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    public static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public static <T, R> PageResponse<R> slice(List<T> source,
                                               Integer page,
                                               Integer size,
                                               Function<T, R> mapper) {
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        int total = source == null ? 0 : source.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<R> items = total == 0
                ? Collections.emptyList()
                : source.subList(fromIndex, toIndex).stream().map(mapper).collect(Collectors.toList());
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

        PageResponse<R> response = new PageResponse<>();
        response.setItems(items);
        response.setTotal(total);
        response.setPage(safePage);
        response.setSize(safeSize);
        response.setTotalPages(totalPages);
        response.setHasNext(safePage < totalPages);
        response.setHasPrevious(safePage > 1 && totalPages > 0);
        return response;
    }
}
