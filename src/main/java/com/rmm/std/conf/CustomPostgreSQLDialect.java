package com.rmm.std.conf;

import com.rmm.std.constant.PromotionStatus;
import org.hibernate.dialect.PostgreSQLDialect;

public class CustomPostgreSQLDialect extends PostgreSQLDialect {

  @Override
  public String getEnumTypeDeclaration(Class<? extends Enum<?>> enumType) {
    if (enumType == PromotionStatus.class) {
      return "promotion_status";
    }
    return super.getEnumTypeDeclaration(enumType);
  }
}
