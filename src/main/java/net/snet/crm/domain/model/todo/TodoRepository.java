package net.snet.crm.domain.model.todo;

import net.snet.crm.domain.shared.data.Data;

import java.util.List;

public interface TodoRepository
{
  List<Data> findServiceComments(long serviceId);

  Data findTodo(long todoId);
}
