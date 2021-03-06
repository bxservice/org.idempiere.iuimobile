/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.mobile;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.Element;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.select;
import org.apache.ecs.xhtml.textarea;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.Lookup;
import org.compiere.model.MLocator;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.NamePair;
import org.compiere.util.Util;
import org.compiere.util.ValueNamePair;

/**
 *	Web Field.
 *	
 *  @author Jorg Janke
 *  @version $Id: WebField.java,v 1.1 2009/04/15 11:27:15 vinhpt Exp $
 */
public class WebField
{
	protected static CLogger	log = CLogger.getCLogger(WWindow.class);
	/**
	 * 	Web Field
	 *	@param wsc session context
	 *	@param columnName column
	 *	@param name label
	 *	@param description description
	 *	@param displayType display type
	 *	@param fieldLength field length
	 *	@param displayLength optional display length
	 *	@param longField if true spans 3 columns
	 *	@param readOnly read only
	 *	@param mandatory mandatory
	 *	@param error erro status
	 *	@param hasDependents has dependent fields
	 *	@param hasCallout has callout functions
	 */
	public WebField (MobileSessionCtx wsc,
		String columnName, String name, String description,
		int displayType, int fieldLength, int displayLength, boolean longField, 
		boolean readOnly, boolean mandatory, boolean error, 
		boolean hasDependents, boolean hasCallout, int AD_Process_ID,
		int AD_Window_ID, int AD_Record_ID, int AD_Table_ID, int fieldNumber, Object defaultvalue, 
		String callOut, GridTab mTab, GridField mField, MRole mRole, boolean rangeTo)
	{
		super ();
		m_wsc = wsc;
		m_columnName = columnName;		
		if (rangeTo)
			m_columnName += "_2";
		if (name == null || name.length() == 0)
			m_name = columnName;
		else
			m_name = name;
		if (rangeTo)
			m_name += " - " + Msg.getMsg(wsc.ctx, "To");
		if (description != null && description.length() > 0)
			m_description = description;
		//
		m_defaultObject = defaultvalue; 
		m_displayType = displayType;
		m_processID = AD_Process_ID;
		m_windowID = AD_Window_ID;
		m_tableID = AD_Table_ID;
		m_recordID = AD_Record_ID;
		m_fieldLength = fieldLength;
		m_displayLength = displayLength;
		if (m_displayLength <= 22)
			m_displayLength = 22;	//	default length
		else
			m_displayLength = 44;	//	default length
		m_longField = longField;
		//
		m_readOnly = readOnly;
		m_mandatory = mandatory;
		m_error = error;
		m_hasDependents = hasDependents;
		m_hasCallout = hasCallout;
		m_callOut = callOut;
		m_fieldNumber = fieldNumber;
		m_Tab = mTab;
		m_Field = mField;
		m_Role = mRole;
		if (rangeTo)
			m_SuffixTo = "T";
		//
		
	}	//	WebField
	
	public WebField (MobileSessionCtx wsc,
			String columnName, String name, String description,
			int displayType, int fieldLength, int displayLength, boolean longField, 
			boolean readOnly, boolean mandatory, boolean error, 
			boolean hasDependents, boolean hasCallout, int AD_Process_ID,
			int AD_Window_ID, int AD_Record_ID, int AD_Table_ID, int fieldNumber, Object defaultvalue, 
			String callOut, GridTab mTab, GridField mField, MRole mRole) {
		this(wsc,
				columnName, name, description,
				displayType, fieldLength, displayLength, longField, 
				readOnly, mandatory, error, 
				hasDependents, hasCallout, AD_Process_ID,
				AD_Window_ID, AD_Record_ID, AD_Table_ID, fieldNumber, defaultvalue, 
				callOut, mTab, mField, mRole, false);
	}

	/**	CSS Field Mandatory Class				*/
	public static final String C_MANDATORY = "Cmandatory";
	/**	CSS Field Error Class					*/
	public static final String C_ERROR     = "Cerror";

	/** 	Web Session Context		*/
	private MobileSessionCtx		m_wsc;
	
	private String 	m_columnName;
	private String	m_name;
	private String	m_description;
	@SuppressWarnings("unused")
	private String  m_callOut;
	@SuppressWarnings("unused")
	private GridTab m_Tab;
	private GridField m_Field;
	private MRole m_Role;
	private String m_SuffixTo = "";
	//
	private Object	m_defaultObject;
	private int		m_displayType;
	private int		m_processID;
	@SuppressWarnings("unused")
	private int		m_windowID;
	@SuppressWarnings("unused")
	private int		m_tableID;
	@SuppressWarnings("unused")
	private int		m_recordID;
	private int		m_fieldLength;
	private int		m_displayLength;
	@SuppressWarnings("unused")
	private boolean	m_longField;
	//
	private boolean	m_readOnly;
	private boolean	m_mandatory;
	private boolean	m_error;
	private boolean	m_hasDependents;
	private boolean	m_hasCallout;
	private int	m_fieldNumber;
	private Object	m_dataDisplay;
	//Modified by Rob Klein 4/29/07
	
	private Lookup m_lookup;
	
	/**
	 * 	Get the field Label
	 * @param edit TODO
	 *	@return label
	 */
	public Element getLabel(boolean edit)
	{
		if (m_displayType == DisplayType.Button)
			return new label();
		//
		label myLabel = new label(m_columnName + "F" + m_SuffixTo, null, StringEscapeUtils.escapeHtml(m_name));
		myLabel.setID(m_columnName + "L" + m_SuffixTo);
		if (m_description != null)
			myLabel.setTitle(StringEscapeUtils.escapeHtml(m_description));
		if ( edit && m_readOnly )
			myLabel.setClass("readonly");
		else if ( edit && m_mandatory )
			myLabel.setClass("mandatory");
		
		//
		return myLabel;
	}	//	getLabel
	

	
	/**
	 * 	Get Field
	 *	@param lookup lookup
	 *	@param data data
	 *	@return field
	 */
	public Element getField (Lookup lookup, Object data)
	{
		m_lookup=lookup;
		String dataValue = (data == null) ? "" : data.toString();
		
		//
		if (m_displayType == DisplayType.Search
			|| m_displayType == DisplayType.Location
			|| m_displayType == DisplayType.Account
			|| m_displayType == DisplayType.PAttribute)
		{
			
			if ( m_readOnly )
			{
				return getDiv(lookup.getDisplay(data));
			}
			String dataDisplay = "";
			if (lookup != null && data != null)
				dataDisplay = lookup.getDisplay(data);
			return getPopupField (dataDisplay, dataValue);
		}

		if (DisplayType.isLookup(m_displayType) 
				|| m_displayType == DisplayType.Locator 
				// By Syed - syed@syvasoft.com
				// added Payment data type to show as dropdown
				|| m_displayType == DisplayType.Payment	) {	

			if ( m_readOnly )
			{
				return getDiv(lookup.getDisplay(data));
			}
			return getSelectField(lookup, dataValue);
		}

		if (m_displayType == DisplayType.YesNo){			
			return getCheckField (dataValue);}

		if (m_displayType == DisplayType.Button){
			return getButtonField ();
		}
		//Modified by Rob Klein 4/29/07
		if (DisplayType.isDate(m_displayType))
		{
			return getPopupDateField(data);
		}
		else if (DisplayType.isNumeric(m_displayType)){
			return getNumberField(data);
		}
		
		if (m_displayType == DisplayType.Text || m_displayType == DisplayType.TextLong){

			if ( m_readOnly )
			{
				return getDiv(dataValue);
			}
			return getTextField (dataValue, 10);
		}
		else if (m_displayType == DisplayType.Memo){

			if ( m_readOnly )
			{
				return getDiv(dataValue);
			}
			return getTextField (dataValue, 10);
		}
		
		//other
		//if (m_displayType == DisplayType.PAttribute){
		//		return getPopupField(dataDisplay, dataValue);}
		
		if (m_displayType == DisplayType.Assignment){
				return getAssignmentField(data);}
		return getStringField(dataValue);
	}	//	getField

	private Element getDiv (String data)
	{

		if ( m_columnName.toLowerCase().contains("phone") && data != null)
		{
				a a = new a("tel:"+data, data);
				a.setTarget("_self");
				return a;
		}
		else if ( m_columnName.toLowerCase().contains("email")&& data != null)
		{
				a a = new a("mailto:"+data, data);
				a.setTarget("_self");
				return a;
		}
		else if ( m_displayType == DisplayType.URL && data != null)
		{
				a a = new a(data, data);
				a.setTarget("_self");
				return a;
		}
		else if (m_displayType == DisplayType.Location && data != null)
		{
			try {
				String map = "geo:0,0?q="
					+ URLEncoder.encode(data,"UTF-8");

				a a = new a(map, data);
				a.setTarget("_self");
				return a;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				div d = new div();
				d.setClass("fieldValue");
				d.addElement(data);
				return d;
			}
		}
		else {
			div d = new div();
			d.setClass("fieldValue");
			d.addElement(data);
			return d;
		}
	}
	
	/**
	 * 	Create String Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Element getStringField (String data)
	{
		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(data));
		
		input string = null;
		
		Boolean isEncrypted = false;
		
		if(m_Field != null)
			isEncrypted = m_Field.isEncryptedField();
			
		
		if(isEncrypted)
			string = new input(input.TYPE_PASSWORD, m_columnName, StringEscapeUtils.escapeHtml(data));
		else if ( m_columnName.toLowerCase().contains("email") )
			string = new input("email", m_columnName, StringEscapeUtils.escapeHtml(data));
		else if ( m_columnName.toLowerCase().contains("phone") && data != null)
			string = new input("phone", m_columnName, StringEscapeUtils.escapeHtml(data));
		else if ( m_displayType == DisplayType.URL )
			string = new input("url", m_columnName, StringEscapeUtils.escapeHtml(data));
		else
			string = new input(input.TYPE_TEXT, m_columnName, StringEscapeUtils.escapeHtml(data));
		
		
		string.setID(m_columnName + "F" + m_SuffixTo);
		//string.setSize(m_displayLength);
		if (m_fieldLength > 0)
			string.setMaxlength(m_fieldLength);
		//
		string.setDisabled(m_readOnly);
		if (m_error)
			string.setClass(C_ERROR);
		else if (m_mandatory)
		{
			string.setClass(C_MANDATORY);
			string.addAttribute("required", "");
		}
		//
		if (m_hasDependents || m_hasCallout)
			string.setOnChange("startUpdate(this);");
//			string.setOnChange("dynDisplay();");
		//
		return string;
	}	//	getStringField
	
	/**
	 * 	Create Text Field
	 * 	@param data initial value
	 * 	@param rows no of rows
	 *	@return td
	 */
	private Element getTextField (String data, int rows)
	{

		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(data));
		
		textarea text = new textarea (m_columnName, rows, m_displayLength)
			.addElement(StringEscapeUtils.escapeHtml(data));
		text.setID(m_columnName + "F" + m_SuffixTo);
		text.setDisabled(m_readOnly);
		if (m_error)
			text.setClass(C_ERROR);
		else if (m_mandatory)
			text.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			text.setOnChange("startUpdate(this);");
		//
		return text;
	}	//	getTextField
		
	/**
	 * 	Create Date Field
	 * 	@param data initial value
	 *	@return td
	 */
	@SuppressWarnings("unused")
	private Element getDateField (Object data)
	{
		String formattedData = "";
		if (data == null)
			;
		else if (m_displayType == DisplayType.DateTime)
			formattedData = m_wsc.dateTimeFormat.format(data); 
		else
			formattedData = m_wsc.dateFormat.format(data);
		

		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(formattedData));

		input string = new input(input.TYPE_TEXT, m_columnName, formattedData);
		string.setID(m_columnName + "F" + m_SuffixTo);
		//string.setSize(m_displayLength);
		if (m_fieldLength > 0)
			string.setMaxlength(m_fieldLength);
		//
		string.setDisabled(m_readOnly);
		if (m_error)
			string.setClass(C_ERROR);
		else if (m_mandatory)
			string.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			string.setOnChange("startUpdate(this);");
		//
		return string;
	}	//	getDateField
	
	/**
	 * 	Create Assignment Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Element getAssignmentField (Object data)
	{
		
		input string = new input(input.TYPE_TEXT, m_columnName, StringEscapeUtils.escapeHtml(""));
		if (m_fieldLength > 0)
			string.setMaxlength(m_fieldLength);
		//
		string.setDisabled(true);
		if (m_error)
			string.setClass(C_ERROR);
		else if (m_mandatory)
			string.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			string.setOnChange("startUpdate(this);");
		return string.addElement("Not Yet Supported");
	}
	
	
	/**
	 * 	Create Number Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Element getNumberField (Object data)
	{
		String formattedData = "";		
//Modified by Rob Klein 4/29/07
		if (data == null)
			if (m_displayType == DisplayType.Amount	)
				formattedData = m_wsc.amountFormat.format(0.00);
			else if (m_displayType == DisplayType.Number
				|| m_displayType == DisplayType.CostPrice )
				formattedData = m_wsc.numberFormat.format(0.00);
			else if (m_displayType == DisplayType.Integer)
				formattedData = m_wsc.integerFormat.format(0);
			else
				formattedData = "0";
		//else if (m_displayType == DisplayType.Amount)			
				//formattedData = m_wsc.amountFormat.format(data);			
		//else if (m_displayType == DisplayType.Number
			//|| m_displayType == DisplayType.CostPrice)
			//formattedData = m_wsc.numberFormat.format(data);
		else if (m_displayType == DisplayType.Quantity)
			formattedData = m_wsc.quantityFormat.format(data);
		else if (m_displayType == DisplayType.Integer)
			formattedData = m_wsc.integerFormat.format(data);
		else
			formattedData = data.toString();
		//
		

		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(formattedData));
			
		
		input string = new input("text", m_columnName, formattedData);
		
		string.setID(m_columnName + "F" + m_SuffixTo);
		//string.setSize(m_displayLength);
		if (m_fieldLength > 0)
			string.setMaxlength(m_fieldLength);
		//
		string.setDisabled(m_readOnly);
		if (m_error)
			string.setClass(C_ERROR);
		else if (m_mandatory)
			string.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			string.setOnChange("startUpdate(this);");
		//
		string.setOnKeyPress("return inputNumber(event);");
		
		
		return string;
	}	//	getNumberField
	
	
	/**
	 * 	Create Checkbox Field
	 * 	@param data initial value
	 *	@return td
	 */
	private Element getCheckField (String data)
	{
		boolean check = data != null 
			&& (data.equals("true") || data.equals("Y"));
		//
		input cb = new input (input.TYPE_CHECKBOX, m_columnName, "true")
			.setChecked(check);
		cb.setID(m_columnName + "F" + m_SuffixTo);
		cb.setDisabled(m_readOnly);
		if (m_error)
			cb.setClass(C_ERROR);
		
		if (m_hasDependents || m_hasCallout)
			cb.setOnChange("startUpdate(this);");
		//
		return cb;
	}	//	getCheckField

	/**
	 * 	Get Popup Field (lookup, location, account, ..)
	 *	@param dataDisplay data to be displayed
	 *	@param dataValue data of value field
	 *	@return td
	 */
	private Element getPopupField (String dataDisplay, String dataValue)
	{
		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(dataDisplay));
		
		if ( Util.isEmpty(dataDisplay))
			dataDisplay = "Select...";
		//  The hidden data field        Name=columnName
		input hidden = new input (input.TYPE_HIDDEN, m_columnName, dataValue);
		hidden.setID(m_columnName + "F" + m_SuffixTo);
		//Modified by Rob Klein 4/29/07
		//input display = null;
		a display = null;
		m_dataDisplay = null;
		//  The display field       Name=columnName, ID=FcolumnName		
			// display = new input(input.TYPE_TEXT, m_columnName + "D",StringEscapeUtils.escapeHtml(dataDisplay));
		
		if (m_displayType == DisplayType.Location ){
			display = new a("WLocation?ColumnName=" + m_columnName, m_columnName + "D", StringEscapeUtils.escapeHtml(dataDisplay));
			//display.addAttribute("onClick", "window.open(WLocation?ColumnName=" + m_columnName + "");
			//display.addAttribute("onClick", "window.open('WLocation?ColumnName="+  m_columnName + "', 'myWindow', 'width=100%, height=200'); ");
		}else 
			display = new a("WLookup?ColumnName=" + m_columnName+"&AD_Process_ID="+m_processID, m_columnName + "D", StringEscapeUtils.escapeHtml(dataDisplay));
			
			
			m_dataDisplay = dataDisplay;
			display.setID(m_columnName + "D" + m_SuffixTo);
			//display.setReadOnly(true);
			//display.setDisabled(m_readOnly);
		
		/*
		if (m_displayType == DisplayType.Location)
			display.setOnClick("startLocation('" + m_columnName + "');return false;");
		else if (m_displayType == DisplayType.Account)
			//modified by rob klein 4/29/07
			display.setOnClick("startLookup('" + m_columnName + "', "+m_processID+", 1);return false;");
		else			
			//modified by rob klein 4/29/07
			display.setOnClick("startLookup('" + m_columnName + "', "+m_processID+", 1);return false;");
		*/
		//Start Popup Menu
		//Add by Rob Klein 6/6/2007		
		@SuppressWarnings("unused")
		a buttonFlyout = null;
		String menu = null;		
		
		/*
		if(m_Field != null)
		{
			menu = getpopUpMenu ();		
			//	Popmenu
			if(menu!=null){
				buttonFlyout = new a("#", "");
				buttonFlyout.addElement(new img(MobileEnv.getImageDirectory("menufly10.gif")).setBorder(0));
				buttonFlyout.setID(m_columnName + "PV" + m_Suffix);
				buttonFlyout.setOnMouseOver("dropdownmenu(this, event, 'menu1["+m_fieldNumber+"]')");
				buttonFlyout.setOnMouseOut("delayhidemenu()");		
			}
		}
		
		*/
		
		if (m_error)
			display.setClass(C_ERROR);
		else if (m_mandatory)
			display.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout){
			
			hidden.setOnChange("startUpdate(this)");
			
		}
		
		//
		if(m_Field != null)
		{
			div popup = new div(menu);
			popup.setClass("anylinkcss");
			popup.setID("menu1["+m_fieldNumber+"]" + m_SuffixTo);
			return hidden	
			.addElement(display);	
			}
		else
		{			
			return hidden
				.addElement(display); 
		}
		
	}	//	getPopupField
	
	
	/**
	 * 	Get Popup Field (lookup, location, account, ..)
	 *	@param dataDisplay data to be displayed
	 *	@param dataValue data of value field
	 *	@return td
	 */
	private Element getPopupDateField ( Object data)
	{
		//  The hidden data field        Name=columnName
		
		String dataValue = (data == null) ? "" : data.toString();	
				
		/*input hidden = new input (input.TYPE_HIDDEN, m_columnName+"F", dataValue);
		hidden.setID(m_columnName + "F"+m_fieldNumber + m_SuffixTo);*/
		input display = null;
		//  The display field       Name=columnName, ID=FcolumnName
		String formattedData = "";
		
		
		try {
			if (data == null)
				;
			else if (m_displayType == DisplayType.DateTime){
				if (dataValue.equals("@#Date@"))
					formattedData = m_wsc.dateTimeFormat.format(new java.util.Date());
				else
					formattedData = m_wsc.dateTimeFormat.format(data);			
				}
			else if (m_displayType == DisplayType.Date){
				if (dataValue.equals("@#Date@"))
					formattedData =  m_wsc.dateFormat.format(new java.util.Date());
				else
					formattedData = m_wsc.dateFormat.format(data);			
				}
		} catch (IllegalArgumentException e) {
			// invalid date format
			formattedData = "Invalid date format: " + data;
		}		
		

		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(formattedData));
		
		display = new input(input.TYPE_TEXT, m_columnName, formattedData);
		display.setID(m_columnName + "F" + m_SuffixTo);
		display.setReadOnly(true);		
		//  The button              Name=columnName, ID=BcolumnName
		input button = new input (input.TYPE_IMAGE, m_columnName+ "B", "x");
		button.setID(m_columnName + "B" + m_SuffixTo);		
		button.setSrc(MobileEnv.getImageDirectory("Calendar10.gif"));
		
		
		String format = m_wsc.dateFormat.toPattern();
		format = format.replaceFirst("d", "%e");
		format = format.replaceFirst("%ed", "%d");
		format = format.replaceFirst("M{1,2}", "%m");
		format = format.replaceFirst("%mM", "%b");
		format = format.replaceFirst("%bM+", "%B");
		format = format.replaceFirst("yyyy", "%Y");
		format = format.replaceFirst("yy", "%y");
		
		if (m_displayType == DisplayType.Date){			
			display.setOnClick("return showCalendar('"+m_columnName+ "F" + m_SuffixTo + "', '"+format+"');"); 
		}
		else if (m_displayType == DisplayType.DateTime){
			display.setOnClick("showCalendar('"+m_columnName+ "F" + m_SuffixTo + "', '"+format+" %H:%M:%S %p', '24');return false;");
		}
		//
		if (m_error)
			display.setClass(C_ERROR);
		else if (m_mandatory)
			display.setClass(C_MANDATORY);
		//
		if (m_hasDependents || m_hasCallout)
			display.setOnChange("startUpdate(this);");
		//
		return display; /*hidden
			.addElement(display); /*
			.addElement(button.setBorder(0));*/
	}	//	getPopupField

	/**
	 * 	Get Select Field
	 *	@param lookup lookup
	 *	@param dataValue default value
	 *	@return selction td
	 */
	private Element getSelectField (Lookup lookup, String dataValue)
	{		
		
		
		if (dataValue.length()<1 && m_defaultObject != null)	{			
			dataValue = m_defaultObject.toString();		
		}		
				
		String dataDisplay = "";
		if (lookup != null && dataValue != null && dataValue.length() > 0)
			dataDisplay = lookup.getDisplay(dataValue);
		
		if ( m_readOnly )
			return getDiv(StringEscapeUtils.escapeHtml(dataDisplay));
				
		option[] ops = getOptions(lookup, dataValue);
		select sel = new select(m_columnName, ops);		
		sel.setID(m_columnName + m_SuffixTo);
		sel.setDisabled(m_readOnly);
		
		if (m_error)
			sel.setClass(C_ERROR);
		else if (m_mandatory)
			sel.setClass(C_MANDATORY);
		
		if (m_hasDependents || m_hasCallout)
			sel.setOnChange("startUpdate(this);");
		
		//Start Setting up popUpMenu
		//Add by Rob Klein 6/6/2007
		a buttonFlyout = null;
		String menu = null;		
		
		if(m_Field != null && !m_readOnly)
		{						
			//	Popmenu create
			menu = getpopUpMenu ();
			if(menu!=null){
			buttonFlyout = new a("#", "");
			buttonFlyout.addElement(new img(MobileEnv.getImageDirectory("menufly10.gif")).setBorder(0));
			buttonFlyout.setID(m_columnName + "PV" + m_SuffixTo);
			buttonFlyout.setOnMouseOver("dropdownmenu(this, event, 'menu1["+m_fieldNumber+"]')");
			buttonFlyout.setOnMouseOut("delayhidemenu()");}
			
		}
		//
		div popup = new div(menu);
		popup.setClass("anylinkcss");
		popup.setID("menu1["+m_fieldNumber+"]" + m_SuffixTo);
		return sel; //.addElement(buttonFlyout).addElement(popup);	
	}	//	getSelectField

	/**
	 * 	Get Array of options
	 *	@param lookup lookup
	 *	@param dataValue default value
	 *	@return selction td
	 */
	private option[] getOptions (Lookup lookup, String dataValue)
	{
		if (lookup == null)
			return new option[0];
		//boolean keyFound = false;
		//
				
		NamePair value = null;
		Object[] list = lookup.getData (m_mandatory, true, !m_readOnly, false, false)
			.toArray();    //  if r/o also inactive
		int size = list.length;		
		option[] options = new option[size];
		
		if (size == 0 && dataValue.length()>0){
			
			value = lookup.getDirect(dataValue, false, false);		
			if (value != null){				
				options = new option[2];
				if (dataValue.length()<1){
					options[0] = new option("-1").addElement("&nbsp;").setSelected(true);
					options[1] = new option(value.getID()).addElement(value.getName());
					m_dataDisplay =value.getName();
					}
				else
				{
					options[0] = new option("-1").addElement("&nbsp;");
					options[1] = new option(value.getID()).addElement(value.getName()).setSelected(true);
					m_dataDisplay =value.getName();
					}
			}
			return options;
		}
		
		for (int i = 0; i < size; i++)
		{
			boolean isNumber = list[0] instanceof KeyNamePair;
			String key = null;
			if (m_displayType == DisplayType.Locator)
			{
				MLocator loc = (MLocator)list[i];
				key = String.valueOf(loc.getM_Locator_ID());
				String name = StringEscapeUtils.escapeHtml(loc.getValue());
				if (dataValue.equals(key)){
					options[i] = new option(key).addElement(name).setSelected(true);
					m_dataDisplay = name;}
				else
					options[i] = new option(key).addElement(name);				
			}
			else if (isNumber)
			{
				KeyNamePair p = (KeyNamePair)list[i];
				key = String.valueOf(p.getKey());
				String name = StringEscapeUtils.escapeHtml(p.getName());
				if (dataValue.equals(key)){
					options[i] = new option(key).addElement(name).setSelected(true);
					m_dataDisplay =name;}
				else
					options[i] = new option(key).addElement(name);			
			}
			else
			{				
				ValueNamePair p = (ValueNamePair)list[i];
				key = p.getValue();
				if (key == null || key.length() == 0)
					key = "??";
				String name = p.getName();
				if (name == null || name.length() == 0)
					name = "???";
				name = StringEscapeUtils.escapeHtml(name);
				if (dataValue.equals(key)){
					options[i] = new option(key).addElement(name).setSelected(true);
					m_dataDisplay =name;}
				else
					options[i] = new option(key).addElement(name);
			}		
			
		}
		
		//If no key found then default to first value
		//if (!keyFound && size>0)
			//options[0].setSelected(true);
		return options;
	}	//	getOptions

	
	/**
	 * 	Get Button Field
	 *	@return Button 
	 */
	private Element getButtonField ()
	{
		/*
		//Modified by Rob Klein 4/29/07
		a button = new a("#", StringEscapeUtils.escapeHtml(m_name));
		button.setClass("whiteButton");		
		return button;*/
		return null;
	}	//	getButtonField
	
	
	/**
	 * 	Get Popup Menu
	 *	@return Menu String 
	 */
	private String getpopUpMenu ()
	{
		//Start Popup Menu
		//Add by Rob Klein 6/6/2007
		a buttonZoom = null;
		a buttonValuePref = null;		
		String menu = null;
		Boolean tableAccess =false;	
		
			if(m_dataDisplay!=null){
			//	Set ValuePreference
			//	Add by Rob Klein 6/6/2007
			buttonValuePref = new a("#", (new img(MobileEnv.getImageDirectory("vPreference10.gif")).setBorder(0))+"  Preference");
			buttonValuePref.setID(m_columnName + "PV" + m_SuffixTo);			
			buttonValuePref.setOnClick("startValuePref(" + m_displayType + ", '"+StringEscapeUtils.escapeHtml(m_dataDisplay.toString())+ "', '"
					+ m_Field.getValue()+ "', '"+m_Field.getHeader()+ "', '"+m_Field.getColumnName()+ "', "
					+ Env.getAD_User_ID(m_wsc.ctx)+ ", " + Env.getAD_Org_ID(m_wsc.ctx) + ", "+Env.getAD_Client_ID(m_wsc.ctx)
					+ ", "+m_Field.getAD_Window_ID()+");return false;");
			menu = ""+buttonValuePref+" \n";	
			}
			
			//Set Zoom			
			StringBuffer sql = null;
			int refID = m_Field.getAD_Reference_Value_ID();
			Object recordID =0;
			if (m_displayType == DisplayType.List ){
				sql = new StringBuffer ("SELECT AD_Table_ID " 
						+ "FROM AD_Table WHERE TableName = 'AD_Reference'");				
				
				recordID = refID;
			}
			else if (refID > 0){
				sql = new StringBuffer ("SELECT AD_Table_ID " 
						+ "FROM AD_Ref_Table WHERE AD_Reference_ID = "+refID);
				recordID =m_Field.getValue();
			}
			else{	
				sql = new StringBuffer ("SELECT AD_Table_ID " 
						+ "FROM AD_Table WHERE TableName = '"+m_columnName.replace("_ID", "")+"'");
				recordID =m_Field.getValue();
			}
			int tableID = DB.getSQLValue(null, sql.toString());		
			
			tableAccess = m_Role.isTableAccess(tableID, false);
			if(tableAccess==true){			
				buttonZoom = new a("#", (new img(MobileEnv.getImageDirectory("Zoom10.gif")).setBorder(0))+"  Zoom");
				buttonZoom.setID(m_columnName + "Z" + m_SuffixTo);			
				buttonZoom.setOnClick("startZoom(" + tableID + ", "+recordID+");return false;");
				if(m_dataDisplay!=null) 
					menu = menu + ""+buttonZoom+"\n";
				else
					menu = ""+buttonZoom+"\n";
					
			}
		return menu;
	}	//	getpopUpMenu
	
	/**************************************************************************
	 *	Create default value.
	 *  <pre>
	 *		(a) Key/Parent/IsActive/SystemAccess
	 *      (b) SQL Default
	 *		(c) Column Default		//	system integrity
	 *      (d) User Preference
	 *		(e) System Preference
	 *		(f) DataType Defaults
	 *
	 *  Don't default from Context => use explicit defaultValue
	 *  (would otherwise copy previous record)
	 *  </pre>
	 *  @return default value or null
	 */
	public Object getDefault(String defaultValue)
	{
		/**
		 *  (a) Key/Parent/IsActive/SystemAccess
		 */

		//	No defaults for these fields m_vo.IsKey ||
		if ( m_displayType == DisplayType.RowID 
			|| DisplayType.isLOB(m_displayType)
			|| "Created".equals(m_columnName) // for Created/Updated default is managed on PO, and direct inserts on DB
			|| "Updated".equals(m_columnName))
			return null;
		//	Always Active
		if (m_columnName.equals("IsActive"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[IsActive] " + m_columnName + "=Y");
			return "Y";
		}

		/**
		 *  (b) SQL Statement (for data integity & consistency)
		 */
		String	defStr = "";
		if (defaultValue != null && defaultValue.startsWith("@SQL="))
		{
			String sql = defaultValue.substring(5);	
			if (sql.equals(""))
			{
				log.log(Level.WARNING, "(" + m_columnName + ") - Default SQL variable parse failed: "
					+ defaultValue);
			}
			else
			{
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try
				{
					stmt = DB.prepareStatement(sql, null);
					rs = stmt.executeQuery();
					if (rs.next())
						defStr = rs.getString(1);
					else
					{
						if (log.isLoggable(Level.INFO))
							log.log(Level.INFO, "(" + m_columnName + ") - no Result: " + sql);
					}
				}
				catch (SQLException e)
				{
					log.log(Level.WARNING, "(" + m_columnName + ") " + sql, e);
				}
				finally
				{
					DB.close(rs, stmt);
					rs = null;
					stmt = null;
				}
			}
			if (defStr != null && defStr.length() > 0)
			{
				if (log.isLoggable(Level.FINE)) log.fine("[SQL] " + m_columnName + "=" + defStr);
				return createDefault(defStr);
			}
		}	//	SQL Statement

		/**
		 * 	(c) Field DefaultValue		=== similar code in AStartRPDialog.getDefault ===
		 */
		if (defaultValue != null && !defaultValue.equals("") && !defaultValue.startsWith("@SQL="))
		{
			defStr = "";		//	problem is with texts like 'sss;sss'
			//	It is one or more variables/constants
			StringTokenizer st = new StringTokenizer(defaultValue, ",;", false);
			while (st.hasMoreTokens())
			{
				defStr = st.nextToken().trim();
				if (defStr.equals("@SysDate@"))				//	System Time
					return new Timestamp (System.currentTimeMillis());
				else if (defStr.indexOf("'") != -1)			//	it is a 'String'
					defStr = defStr.replace('\'', ' ').trim();

				if (!defStr.equals(""))
				{
					if (log.isLoggable(Level.FINE)) log.fine("[DefaultValue] " + m_columnName + "=" + defStr);
					return createDefault(defStr);
				 }
			}	//	while more Tokens
		}	//	Default value
		/**
		 *	(f) DataType defaults
		 */

		//	Button to N
		if (m_displayType == DisplayType.Button && !m_columnName.endsWith("_ID"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[Button=N] " + m_columnName);
			return "N";
		}
		//	CheckBoxes default to No
		if (m_displayType == DisplayType.YesNo)
		{
			if (log.isLoggable(Level.FINE)) log.fine("[YesNo=N] " + m_columnName);
			return "N";
		}
		//  lookups with one value
	//	if (DisplayType.isLookup(m_displayType) && m_lookup.getSize() == 1)
	//	{
	//		/** @todo default if only one lookup value */
	//	}
		//  IDs remain null
		if (m_columnName.endsWith("_ID"))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[ID=null] "  + m_columnName);
			return null;
		}
		//  actual Numbers default to zero
		if (DisplayType.isNumeric(m_displayType))
		{
			if (log.isLoggable(Level.FINE)) log.fine("[Number=0] " + m_columnName);
			return createDefault("0");
		}

		/**
		 *  No resolution
		 */
		if (log.isLoggable(Level.FINE)) log.fine("[NONE] " + m_columnName);
		return null;
	}	//	getDefault
	
	/**
	 *	Create Default Object type.
	 *  <pre>
	 *		Integer 	(IDs, Integer)
	 *		BigDecimal 	(Numbers)
	 *		Timestamp	(Dates)
	 *		Boolean		(YesNo)
	 *		default: String
	 *  </pre>
	 *  @param value string
	 *  @return type dependent converted object
	 */
	private Object createDefault (String value)
	{
		//	true NULL
		if (value == null || value.toString().length() == 0)
			return null;
		//	see also MTable.readData
		try
		{
			//	IDs & Integer & CreatedBy/UpdatedBy
			if (m_columnName.endsWith("atedBy")
					|| (m_columnName.endsWith("_ID") && DisplayType.isID(m_displayType))) // teo_sarca [ 1672725 ] Process parameter that ends with _ID but is boolean
			{
				try	//	defaults -1 => null
				{
					int ii = Integer.parseInt(value);
					if (ii < 0)
						return null;
					return new Integer(ii);
				}
				catch (Exception e)
				{
					log.warning("Cannot parse: " + value + " - " + e.getMessage());
				}
				return new Integer(0);
			}
			//	Integer
			if (m_displayType == DisplayType.Integer)
				return new Integer(value);
			
			//	Number
			if (DisplayType.isNumeric(m_displayType))
				return new BigDecimal(value);
			
			//	Timestamps
			if (DisplayType.isDate(m_displayType))
			{
				// try timestamp format - then date format -- [ 1950305 ]
				java.util.Date date = null;
				SimpleDateFormat dateTimeFormat = DisplayType.getTimestampFormat_Default();
				SimpleDateFormat dateFormat = DisplayType.getDateFormat_JDBC();
				SimpleDateFormat timeFormat = DisplayType.getTimeFormat_Default();
				try {
					if (m_displayType == DisplayType.Date) {
						date = dateFormat.parse (value);
					} else if (m_displayType == DisplayType.Time) {
						date = timeFormat.parse (value);
					} else {
						date = dateTimeFormat.parse (value);
					}
				} catch (java.text.ParseException e) {
					date = DisplayType.getDateFormat_JDBC().parse (value);
				}
				return new Timestamp (date.getTime());
			}
			
			//	Boolean
			if (m_displayType == DisplayType.YesNo)
				return Boolean.valueOf ("Y".equals(value));
			
			//	Default
			return value;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, m_columnName + " - " + e.getMessage());
		}
		return null;
	}	//	createDefault
	
	public String getColumnName()
	{
		return m_columnName;
	}
	public String getFieldName()
	{
		return m_name;
	}
	public boolean isHasDependents()
	{
		return m_hasDependents;
	}

	public boolean isHasCallout()
	{
		return m_hasCallout;
	}
	//public Lookup getLookup()
	//{
	//	return m_lookup;
	//}
	public boolean isMandatory()
	{
		return m_mandatory;
	}
	public Lookup getLookup()
	{
		return m_lookup;
	}
}	//	WebField
