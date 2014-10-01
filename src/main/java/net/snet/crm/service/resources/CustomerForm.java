package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 27.9.14.
 */
public class CustomerForm {
	private static final Map<String, String> COPY_NOT_EMPTY_FIELDS;

	static {
		COPY_NOT_EMPTY_FIELDS = Maps.newLinkedHashMap();
		COPY_NOT_EMPTY_FIELDS.put("supplementary_name", "supplementary_name");
		COPY_NOT_EMPTY_FIELDS.put("public_id", "public_id");
		COPY_NOT_EMPTY_FIELDS.put("dic", "dic");
		COPY_NOT_EMPTY_FIELDS.put("contact_name", "contact_name");
		COPY_NOT_EMPTY_FIELDS.put("email", "email");
		COPY_NOT_EMPTY_FIELDS.put("phone", "phone");
		COPY_NOT_EMPTY_FIELDS.put("info", "info");
		COPY_NOT_EMPTY_FIELDS.put("address.street", "street");
	}

	private final Map<String, Object> formMap;

	public CustomerForm(Map<String, Object> formMap) {
		this.formMap = formMap;
	}

	public Map<String, Object> customerUpdate() {
		final HashMap<String, Object> update = Maps.newHashMap();
		for (Map.Entry<String, String> field : COPY_NOT_EMPTY_FIELDS.entrySet()) {
			final Optional<Object> value = getNested(field.getKey());
			if (value.isPresent()) {
				update.put(field.getValue(), value.get());
			}
		}
		return update;
	}

	@SuppressWarnings("unchecked")
	private Optional<Object> getNested(String path) {
		Object value = formMap;
		try {
			for (String key : Splitter.on('.').split(path)) {
				value = ((Map<String, Object>) value).get(key);
			}
		} catch (Exception e) {
			return Optional.absent();
		}
		if (value != null && !"".equals(value.toString().trim())) {
			return Optional.of(value);
		} else {
			return Optional.absent();
		}
	}
}
