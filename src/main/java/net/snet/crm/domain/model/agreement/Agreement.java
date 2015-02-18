package net.snet.crm.domain.model.agreement;

import net.snet.crm.domain.shared.Entity;

import java.util.Map;

public class Agreement implements Entity<Agreement, AgreementId> {

  public Agreement(Map<String, Object> draft) {

  }

  @Override
  public AgreementId id() {
    return null;
  }
}
