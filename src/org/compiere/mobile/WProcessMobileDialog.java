/******************************************************************************
 * Copyright (C) 2015 tbayen_bxservice                                        *
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package org.compiere.mobile;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.adempiere.util.ServerContext;
import org.apache.ecs.Element;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.body;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.form;
import org.apache.ecs.xhtml.h1;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.td;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_AD_Process;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MToolBarButton;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Msg;

@WebServlet(
		name="WProcessPara",
		urlPatterns = "/WProcessPara"
		)
public class WProcessMobileDialog extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -763641668438890217L;
	protected CLogger	log = CLogger.getCLogger(getClass());

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!MobileEnv.initWeb(config))
			throw new ServletException("WProcessPara.init");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		MobileSessionCtx wsc = MobileSessionCtx.get(request);
		WWindowStatus ws = WWindowStatus.get(request);

		if (wsc == null )
		{
			MobileUtil.createTimeoutPage(request, response, this,  null);
			return;
		}

		String action = MobileUtil.getParameter(request, "action");

		String title = "Process";

		MobileDoc doc = MobileDoc.createPopup(title);
		body body = doc.getBody();

		if( "edit".equals(action) ){
			int windowNo = ws.mWindow.getWindowNo();
			int tableID  = ws.curTab.getAD_Table_ID();
			int recordID = ws.curTab.getRecord_ID();
			int processID = MobileUtil.getParameterAsInt(request, "id");

			body.addElement(createProcessParameterPanel(wsc,ws,processID,windowNo,tableID,recordID,null,null ));
		}
		else{
			body.addElement(creatProcessOptions(wsc, ws));
		}

		MobileUtil.createResponseFragment (request, response, this, null, doc);
	}

	/**
	 * Create the popup with all the button's options to choose 
	 * @param ws
	 * @return
	 */
	private form creatProcessOptions(MobileSessionCtx wsc, WWindowStatus ws){

		form myForm = new form("WProcessPara", form.METHOD_GET);
		myForm.addAttribute("selected", "true");
		myForm.setClass("dialog");
		myForm.setID("WProcessPara");

		fieldset fields = new fieldset();

		h1 h = new h1("Process");
		fields.addElement(h);

		a a = new a("#", Msg.getMsg(wsc.language, "Cancel"));
		a.addAttribute("type", "cancel");
		a.setClass("button leftButton");
		fields.addElement(a);

		if (ws.curTab.isDisplayed())
		{
			int noFields = ws.curTab.getFieldCount();
			for (int i = 0; i < noFields; i++)
			{
				GridField field = ws.curTab.getField(i);
				if ( field.isDisplayed(true) && field.getDisplayType() == DisplayType.Button )
				{
					MProcess process = new MProcess(ws.ctx, field.getAD_Process_ID(), null);
					int processId = process.getAD_Process_ID();
					if(processId>0){
						String processName = process.get_Translation(I_AD_Process.COLUMNNAME_Name);

						a big = new a("WProcessPara?action=edit&id="+processId, processName);
						big.setName(process.getName());
						big.addAttribute("onclick", "(function(event) {return true;})()");
						big.setClass("whiteButton");
						fields.addElement(big);
					}
				}
			}

			MToolBarButton[] toolBarButtons = MToolBarButton.getProcessButtonOfTab(ws.curTab.getAD_Tab_ID(), null);
			if(toolBarButtons != null && toolBarButtons.length >0){
				MProcess process = new MProcess(ws.ctx, toolBarButtons[0].getAD_Process_ID(), null);
				int processId = process.getAD_Process_ID();
				if(processId>0){
					String processName = process.get_Translation(I_AD_Process.COLUMNNAME_Name);
					a big = new a("WProcessPara?action=edit&id="+processId, processName);
					big.setName(process.getName());
					big.addAttribute("onclick", "(function(event) {return true;})()");
					big.setClass("whiteButton");

					fields.addElement(big);
				}
			}
		}

		myForm.addElement(fields);

		return myForm;
	}//creatProcessOptions

	/**
	 * Create the dialog to choose the parameters of the process and run it
	 * @param wsc
	 * @param ws
	 * @param processId
	 * @param windowID
	 * @param tableID
	 * @param recordID
	 * @param columnName
	 * @param mTab
	 * @return
	 */
	public form createProcessParameterPanel(MobileSessionCtx wsc, WWindowStatus ws, int processId, int windowID, int tableID, int recordID, 
			String columnName, GridTab mTab) {

		MProcess process = null;
		ServerContext.setCurrentInstance(wsc.ctx);
		process = MProcess.get(wsc.ctx, processId);

		//	need to check if Role can access
		if (process == null)
		{
			return new form();
		}

		MProcessPara[] parameter = process.getParameters();

		form myForm = new form();
		myForm.addAttribute("selected", "true");
		myForm.setClass("dialog");
		myForm.setMethod(form.METHOD_POST);
		myForm.setID("WProcessPara1");

		fieldset fields = new fieldset();

		h1 h = new h1(process.getName());
		fields.addElement(h);

		a a = new a("#", Msg.getMsg(wsc.language, "Cancel"));
		a.addAttribute("type", "cancel");
		a.setClass("button leftButton");
		fields.addElement(a);

		if(process.getJasperReport() != null && parameter.length == 0){
			a = new a ("WProcess?AD_Process_ID=" + process.getAD_Process_ID()+"&AD_Record_ID="+recordID,Msg.getMsg(wsc.language, "OK"));
			myForm.setAction(MobileEnv.getBaseDirectory("WProcess"));
			myForm.setTarget("_self");
		}
		else{
			a = new a("WProcess", Msg.getMsg(wsc.language, "OK"));
			myForm.setAction(MobileEnv.getBaseDirectory("WProcess"));
		}
		a.setID("okButton");
		a.addAttribute("type", "submit");

		a.addAttribute("onclick", "removeProcessPopups();");
		a.setClass("button");
		fields.addElement(a);


		for (int i = 0; i < parameter.length; i++)
		{
			MProcessPara para = parameter[i];
			WebField wField = new WebField (wsc,
					para.getColumnName(), para.getName(), para.getDescription(),
					//	no display length
					para.getAD_Reference_ID(), para.getFieldLength(), para.getFieldLength(), false,
					// 	not r/o, ., not error, not dependent
					false, para.isMandatory(), false, false, false, para.getAD_Process_ID(),
					0,0,0,i, null,null, null,null, null);

			WebField wFieldforRange = null;

			if(para.isRange())
				wFieldforRange = new WebField (wsc,
						para.getColumnName(), para.getName(), para.getDescription(),
						//	no display length
						para.getAD_Reference_ID(), para.getFieldLength(), para.getFieldLength(), false,
						// 	not r/o, ., not error, not dependent
						false, para.isMandatory(), false, false, false, para.getAD_Process_ID(),0,0,0,i+1,
						null,null, null,null, null);

			Element toField = para.isRange() 
					? wFieldforRange.getField(para.getLookup(), para.getDefaultValue2())
							: new td(MobileEnv.NBSP);

					div d = new div();
					d.setClass("row");
					//Get the default Value of the process
					Object defaultValue = wField.getDefault(para.getDefaultValue());

					//Add to list
					fields.addElement(d
							.addElement(wField.getLabel(true))
							.addElement(wField.getField(para.getLookup(), defaultValue))
							.addElement(toField));
		}

		fields.addElement(new input(input.TYPE_HIDDEN, "AD_Process_ID", process.getAD_Process_ID()));
		fields.addElement(new input(input.TYPE_HIDDEN, "AD_Window_ID", windowID));
		fields.addElement(new input(input.TYPE_HIDDEN, "AD_Table_ID", tableID));
		fields.addElement(new input(input.TYPE_HIDDEN, "AD_Record_ID", recordID));

		myForm.addElement(fields);

		return myForm;
	}//createProcessParameterPanel
}