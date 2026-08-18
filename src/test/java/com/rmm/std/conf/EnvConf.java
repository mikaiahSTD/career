package com.rmm.std.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  public static final String ADMIN_EMAIL = "admin@bootstrap.test";
  public static final String ADMIN_PASSWORD = "Admin@123456";

  public void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.bootstrap-admin.enabled", () -> "true");
    registry.add("app.bootstrap-admin.ref", () -> "ADMIN-BOOTSTRAP");
    registry.add("app.bootstrap-admin.email", () -> ADMIN_EMAIL);
    registry.add("app.bootstrap-admin.firstname", () -> "Admin");
    registry.add("app.bootstrap-admin.lastname", () -> "System");
    registry.add("app.bootstrap-admin.password", () -> ADMIN_PASSWORD);
  }
}
