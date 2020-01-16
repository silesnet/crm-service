package net.snet.api;

import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

enum NodeDTO {;
  private interface Query { @NotBlank String getQuery();}
  private interface Id { Long getId(); }
  private interface Name { @NotBlank String getName(); }
  private interface Master { String getMaster(); }
  private interface Area { String getArea(); }
  private interface Vendor { String getVendor(); }
  private interface Model { String getModel(); }
  private interface LinkTo { String getLinkTo(); }
  private interface RstpNumRing { Integer getRstpNumRing(); }
  private interface BackupPath { String getBackupPath(); }

  enum Request {;
    @Value public static class Query implements NodeDTO.Query {
      final String query;
    }
  }

  enum Response {;
    @Value public static class Node  implements Id, Name, Master, Area, Vendor, Model, LinkTo, RstpNumRing, BackupPath {
      final Long id;
      final String name;
      final String master;
      final String area;
      final String vendor;
      final String model;
      final String linkTo;
      final Integer rstpNumRing;
      final String backupPath;
    }
  }
}
