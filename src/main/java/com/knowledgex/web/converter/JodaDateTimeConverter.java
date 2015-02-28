package com.knowledgex.web.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

@FacesConverter("jodaDateTimeConverter")
public final class JodaDateTimeConverter implements Converter
//    , JsonDeserializer<DateTime>, JsonSerializer<DateTime>
{

	private static final String PATTERN = "yyyy-MM-dd HH-mm";
	
	@Override
	public Object getAsObject(FacesContext ctx, UIComponent component, String value) {
		return DateTimeFormat.forPattern(PATTERN).parseDateTime(value);
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent component, Object value) {
		
		DateTime dateTime = (DateTime) value;
		
		return DateTimeFormat.forPattern(PATTERN).print(dateTime);
	}

//	@Override
//	public JsonElement serialize(DateTime dateTime, Type typeOfSrc, JsonSerializationContext context) {
//		String retVal;
//	    if (dateTime == null) {
//	        retVal = "";
//	    }
//	    else {
//	        retVal = DateTimeFormat.forPattern(PATTERN).print(dateTime);
//	    }
//	    return new JsonPrimitive(retVal);
//	}
//
//	@Override
//	public DateTime deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
//			throws JsonParseException
//	{
//		final String dateTimeAsString = je.getAsString();
//		if (!dateTimeAsString.isEmpty()) {
//			return DateTimeFormat.forPattern(PATTERN).parseDateTime(dateTimeAsString);
//		}
//		return null;
//	}	
	
}
