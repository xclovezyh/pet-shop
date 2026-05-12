package com.petshop.support;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 管理员账号已切换到独立 admin_user 表，这里保留空实现以兼容现有启动流程。
    }
}
