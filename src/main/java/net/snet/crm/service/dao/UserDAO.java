package net.snet.crm.service.dao;

import net.snet.crm.service.bo.User;
import net.snet.crm.service.mapper.UserMapper;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(UserMapper.class)
public interface UserDAO {

    @SqlQuery("SELECT * FROM users WHERE passwd IS NOT NULL ORDER BY name")
    Iterator<User> allUsers();

    void close();
}


