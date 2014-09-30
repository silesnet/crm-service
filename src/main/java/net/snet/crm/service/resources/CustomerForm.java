package net.snet.crm.service.resources;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by admin on 27.9.14.
 */
public class CustomerForm {
	private static final Map<String, String> FIELDS;

	static {
		FIELDS = Maps.newLinkedHashMap();
		FIELDS.put("name", "name");
		FIELDS.put("supplementary_name", "supplementary_name");
		FIELDS.put("public_id", "public_id");
		FIELDS.put("dic", "dic");
		FIELDS.put("representative", "contact_name");
		FIELDS.put("email", "email");
		FIELDS.put("email", "email");
		FIELDS.put("email", "email");
		FIELDS.put("email", "email");
		FIELDS.put("email", "email");
	}


	public CustomerForm(Map<String, Object> formMap) {
		// TODO: implement
	}

	public Map<String, Object> customerUpdate() {
		// TODO: implement
		return Maps.newHashMap();
	}
}
