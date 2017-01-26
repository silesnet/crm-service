package net.snet.crm.infrastructure.persistence.jdbi.support;

import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import org.skife.jdbi.v2.BaseResultSetMapper;

import java.util.Map;

public class DataMapper extends BaseResultSetMapper<Data>
{
  public static final DataMapper INSTANCE = new DataMapper();

  @Override
  protected Data mapInternal(int index, Map<String, Object> row) {
    return row == null ? MapData.EMPTY : MapData.of(row);
  }
}
