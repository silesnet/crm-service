package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.mapper.DraftMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(DraftMapper.class)
public interface DraftDAO {

    @SqlQuery("SELECT * FROM drafts WHERE user_id = :user_id and type = 'service' ORDER BY id DESC")
    Iterator<Draft> findDraftsByUserId(@Bind("user_id") long user_id);

    @SqlQuery("SELECT * FROM drafts WHERE id = :id")
    Draft findDraftById(@Bind("id") long id);

    @SqlUpdate("DELETE FROM drafts WHERE id = :id")
    void deleteDraftById(@Bind("id") long id);

    @SqlUpdate("INSERT INTO drafts (type, user_id, data) values (:type, :userId, :data)")
    @GetGeneratedKeys
    Integer insertDraft(@BindBean Draft draft);

    @SqlUpdate("UPDATE drafts set data = :data WHERE id = :id")
    void updateDraft(@Bind("data") String data, @Bind("id") long id);

    void close();
}


