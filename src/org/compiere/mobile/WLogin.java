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

import java.io.IOException;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ecs.Element;
import org.apache.ecs.HtmlColor;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.b;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.font;
import org.apache.ecs.xhtml.form;
import org.apache.ecs.xhtml.h1;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.script;
import org.apache.ecs.xhtml.select;
import org.apache.ecs.xhtml.strong;
import org.apache.ecs.xhtml.td;
import org.compiere.model.MRole;
import org.compiere.model.MSession;
import org.compiere.model.MSysConfig;
import org.compiere.model.MSystem;
import org.compiere.model.MUser;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Language;
import org.compiere.util.Login;
import org.compiere.util.Msg;
import org.compiere.util.Util;

/**
 *  Web Login Page.
 *  <p>
 *  Page request:
 *  <pre>
 *  - Check database connection
 *  - LoginInfo from request?
 *      - Yes: DoLogin success ?
 *          - Yes: return (second) preferences page
 *          - No: return (first) user/password page
 *      - No: User Principal ?
 *          - Yes: DoLogin success ?
 *              - Yes: return (second) preferences page
 *              - No: return (first) user/password page
 *          - No: return (first) user/password page
 *  </pre>
 *
 *  @author Jorg Janke
 *  @version  $Id: WLogin.java,v 1.1 2009/04/15 11:27:15 vinhpt Exp $
 */
@WebServlet(
		name="WLogin",
        urlPatterns = "/WLogin"
)
public class WLogin extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5241051762495956961L;

	/**	Logger			*/
	protected CLogger	log = CLogger.getCLogger(getClass());
	
	/**
	 *	Initialize
	 *  @param config confif
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!MobileEnv.initWeb(config))
			throw new ServletException("WLogin.init");
	}	//	init

	/**
	 * Get Servlet information
	 * @return servlet info
	 */
	public String getServletInfo()
	{
		return "iDempiere Web Login";
	}	//	getServletInfo

	/**
	 *	Clean up
	 */
	public void destroy()
	{
		log.info("destroy");
		super.destroy();
	}	//	destroy


	/**
	 *	Process the HTTP Get request - forward to Post
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException
	{
		log.info("");
		doPost (request, response);
	}	//	doGet

	//Display Select Role window
		boolean selectRole = true;
	/**
	 *	Process the HTTP Post request.
	 *  <pre>
	 *  - Optionally create Session
	 *  - Check database connection
	 *  - LoginInfo from request?
	 *      - Yes: DoLogin success ?
	 *          - Yes: return (second) preferences page
	 *          - No: return (first) user/password page
	 *      - No: User Principal ?
	 *          - Yes: DoLogin success ?
	 *              - Yes: return (second) preferences page
	 *              - No: return (first) user/password page
	 *          - No: return (first) user/password page
	 *  </pre>
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
	{
		log.info("");
		//  Create New Session
		HttpSession sess = request.getSession(true);
		sess.setMaxInactiveInterval(MobileEnv.TIMEOUT);

		//  Get Cookie Properties
		Properties cProp = MobileUtil.getCookieProprties(request);

		//  Create Context
		MobileSessionCtx wsc = MobileSessionCtx.get (request);

		//  Page
		MobileDoc doc = null;

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(wsc.ctx, "WLoginNoDB");
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			doc = MobileDoc.createWindow (msg);
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = MobileUtil.getParameter (request, WLogin.P_ROLE);
		String client = MobileUtil.getParameter (request, WLogin.P_CLIENT);
		String org = MobileUtil.getParameter (request, WLogin.P_ORG);
		if ( role != null && client != null && org != null )
		{
			createMenu(request,response,wsc,role,client,org,cProp); 
			return;
		}
		//  Login Info from request?
		else
		{
			//  Get Parameters:     UserName/Password
			APP_USER = MobileUtil.getParameter (request, P_USERNAME);
			String pwd = MobileUtil.getParameter (request, P_PASSWORD);
			//  Get Principle
			Principal userPrincipal = request.getUserPrincipal();
			log.info("Principal=" + userPrincipal + "; User=" + APP_USER);

			//  Login info not from request and not pre-authorized
			if (userPrincipal == null && (APP_USER == null || pwd == null)){
				selectRole = true;
				doc = createFirstPage (cProp, request, "");
				String isRemember = cProp.getProperty(P_REMEMBER);
				if(isRemember!=null && isRemember.equals("true")){
					int AD_User_ID = Integer.parseInt(cProp.getProperty(P_USER));
					MUser user = MUser.get(Env.getCtx(), AD_User_ID);
					if (user != null && user.get_ID() == AD_User_ID)
					{
						String userName;
						if (user.getLDAPUser() != null && user.getLDAPUser().length() > 0)
							userName = user.getLDAPUser();
					    else 
							userName = user.getName();
						
						if (MSystem.isZKRememberUserAllowed()) {
							usrInput.setValue(userName);
						}

						/*
						if (MSystem.isZKRememberPasswordAllowed()) {
							// TODO: Encrypt password in cookie instead of reading from user
							pwdInput.setValue(user.getPassword()); // this is not valid - can be hacked editing the cookie and assigning userId=100
						}
						*/

						/*
						// TODO: Validates if the remembered user is a simple user (1 org, 1 client, 1 role) - autologin? cannot logout
						APP_USER = userName;
						KeyNamePair[] clients = null;
						KeyNamePair[] roles   = null;
						Login login = new Login(wsc.ctx);
						clients = login.getClients (userName,user.getPassword());
						if (clients != null){
							setUserID(wsc.ctx, clients[0].getKey());
							roles = WLogin.filterMobileRoles(login.getRoles(userName, clients[0]));
							if(clients.length==1 && roles.length==1 && login.getOrgs(roles[0]).length==1){
								myForm.setTarget("_self");
								selectRole = false;
							}
						}
						*/
						pwd = "";
					}
				}
			}
			//  Login info from request or authorized
			else
			{
				//TODO red1 here starts the need to swap Role with Client DONE
				MobileUtil.getParameter (request, WLogin.P_ROLE);

				KeyNamePair[] clients = null;
				KeyNamePair[] roles   = null;
				Login login = new Login(wsc.ctx);
				clients = login.getClients (APP_USER,pwd);

				//  Pre-authorized
				if (userPrincipal != null)
					APP_USER = userPrincipal.getName();

				if (clients == null){
					selectRole = true; 
					cProp.setProperty(P_REMEMBER, "false");
					doc = createFirstPage(cProp, request, Msg.getMsg(wsc.ctx, "UserPwdError"));
				}
				else
				{
					setUserID(wsc.ctx, clients[0].getKey());
					roles = login.getRoles(APP_USER, clients[0]);
					if(clients.length==1 && roles.length==1 && login.getOrgs(roles[0]).length==1 && !selectRole){
						//If is a simple user use a _self target form if it's not use a normal form

						role   = roles[0].getID();
						client = clients[0].getID();
						org    = login.getOrgs(roles[0])[0].getID();
						//Create adempiere Session - user id in ctx
						MSession.get (wsc.ctx, request.getRemoteAddr(), 
								request.getRemoteHost(), sess.getId() );

						createMenu(request,response,wsc,role,client,org,cProp);
						return;
					}
					else{
						String isRemember =  MobileUtil.getParameter(request, P_REMEMBER);
						rememberCk.setValue(isRemember);
						String roleData=(cProp.getProperty(P_CLIENT, null));
						doc = createSecondPage(cProp, request, clients, roleData, APP_USER, "");

						//	Create adempiere Session - user id in ctx
						MSession.get (wsc.ctx, request.getRemoteAddr(), 
								request.getRemoteHost(), sess.getId() );
						MobileUtil.createResponseFragment (request, response, this, cProp, doc);
						return;
					}
				}

			}
		}
		MobileUtil.createResponse (request, response, this, cProp, doc, false);
	}	//	doPost
	
	private void createMenu(HttpServletRequest request, HttpServletResponse response, MobileSessionCtx wsc, String role, String client, String org, Properties cProp) throws ServletException, IOException
	{
		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getAD_User_ID(wsc.ctx);
		int AD_Role_ID = Env.getAD_Role_ID(wsc.ctx);
		int AD_Client_ID = Env.getAD_Client_ID(wsc.ctx);
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				log.fine("AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(wsc.ctx, "#AD_Role_ID", AD_Role_ID);
			}
			log.fine("AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				log.fine("AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(wsc.ctx, "#AD_Client_ID", AD_Client_ID);
			}
			log.fine("AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			log.fine("AD_Org_ID = " + AD_Org_ID);
			//
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Parameter", e);
			MobileUtil.createTimeoutPage(request, response, this, 
					Msg.getMsg(wsc.ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		wsc.loginInfo = checkLogin (wsc.ctx, AD_User_ID, AD_Role_ID, 
				AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (wsc.loginInfo == null)
		{
			MobileUtil.createErrorPage (request, response, this,  
					Msg.getMsg(wsc.ctx, "RoleInconsistent"));
			return;
		}

		//  Set Date
		Timestamp ts = MobileUtil.getParameterAsDate (request, WLogin.P_DATE);
		if (ts == null)
			ts = new Timestamp(System.currentTimeMillis());
		Env.setContext(wsc.ctx, "#Date", ts);    //  JDBC format

		cProp.setProperty(P_ROLE, Integer.toString(AD_Role_ID));
		cProp.setProperty(P_ORG, Integer.toString(AD_Org_ID));
		cProp.setProperty(P_CLIENT, Integer.toString(AD_Client_ID));
		cProp.setProperty(P_USER, Integer.toString(AD_User_ID));
		cProp.setProperty(P_REMEMBER, rememberCk.getAttribute("value"));
		cProp.setProperty(Env.LANGUAGE, wsc.language.getAD_Language());

		//  Update Cookie - overwrite
		if (cProp != null)
		{
			MobileUtil.updateCookieMobileUser(request, response, cProp);
		}

		response.sendRedirect(MobileEnv.getBaseDirectory("/WMenu"));
	}

	//  Variable Names
	private static final String		P_USERNAME      = "User";
	private static final String		P_PASSWORD      = "Password";
	//private static final String		P_SUBMIT        = "Submit";
	private static final String		P_REMEMBER      = "RememberMe";
	//  WMenu picks it up
	protected static final String   P_USER          = "AD_User_ID"; 
	protected static final String   P_ROLE          = "AD_Role_ID";
	protected static final String   P_CLIENT        = "AD_Client_ID";
	protected static final String   P_ORG           = "AD_Org_ID";
	protected static final String   P_DATE          = "Date";
	protected static final String   P_WAREHOUSE     = "M_Warehouse_ID";
	protected static final String   P_ERRORMSG      = "ErrorMessage";
	protected static final String   P_STORE         = "SaveCookie";
	protected static final String	P_LANGUAGE		= "Language";
	protected static String APP_USER = "";
	/**
	 *  Check Login information and set context.
	 *  @return    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_Role r "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND r.AD_Role_ID=?"    //  #4
			+ " AND o.IsActive='Y' "
			+ " AND o.AD_Client_ID IN (0, c.AD_Client_ID)"
			+ " AND (r.IsAccessAllOrgs='Y'" 
			+ " OR (r.IsUseUserOrgAccess='N' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra" 
			+ " WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) "
			+ " OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua" 
			+ " WHERE ua.AD_User_ID=u.AD_User_ID"
			+ " AND ua.IsActive='Y')))";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		Login login = new Login(ctx);
		login.loadPreferences(org, wh, date, printer);
		//	Don't Show Acct/Trl Tabs on HTML UI
		Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", "N");	
		//
		return loginInfo;
	}   //  checkLogin
	
	private input rememberCk;
	private input usrInput;
	private input pwdInput;
	private form  myForm;
	
	/**************************************************************************
	 *  First Login Page
	 *  @param cProp Login Cookie information for defaults
	 *  @param request request
	 *  @param errorMessage error message
	 *  @return WDoc page
	 *  
	 */
	private MobileDoc createFirstPage(Properties cProp, HttpServletRequest request,
		String errorMessage)
	{
		/** 
		 * Check if login is by user or by email
		 */
		
		
		
		//config=new MSysConfig(Env.getCtx(), AD_SysConfig_ID, trxName)
		log.info (" - " + errorMessage);
		String AD_Language = (cProp.getProperty(Env.LANGUAGE, Language.getAD_Language(request.getLocale())));
		//
		String windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		if(MSysConfig.getValue("USE_EMAIL_FOR_LOGIN").compareTo("Y")==0)
			usrText = Msg.getMsg(AD_Language, "EMail");
		
		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");

		//	Form - post to same URL
		String action = request.getRequestURI();
		myForm = null;
		myForm = new form(action).setName("Login1");
		myForm.setID(windowTitle);
		myForm.setTitle(windowTitle);
		myForm.addAttribute("selected", "true");
		myForm.setClass("panel");
		myForm.setMethod("post");
		myForm.addAttribute("autocomplete", "off");
		myForm.addAttribute("target", "noself"); 
		
		fieldset fs = new fieldset();
		div div1 = new div();
		div1.setClass("row");
		
		//	Username
		label usrLabel = new label().setFor(P_USERNAME + "F").addElement(usrText);
		usrLabel.setID(P_USERNAME + "L");
		div1.addElement(usrLabel);
		usrInput = new input(input.TYPE_TEXT, P_USERNAME, APP_USER).setSize(20).setMaxlength(30);
		usrInput.setID("username");
		usrInput.setOnChange("changeUserName();");
		div1.addElement(usrInput);
		fs.addElement(div1);

		div1 = new div();
		div1.setClass("row");
		
		//  Password
		String pwdData = cProp.getProperty(P_PASSWORD, "");
		label pwdLabel = new label().setFor(P_PASSWORD + "F").addElement(pwdText);
		pwdLabel.setID(P_PASSWORD + "L");
		div1.addElement(pwdLabel);
		pwdInput = new input(input.TYPE_PASSWORD, P_PASSWORD, pwdData).setSize(20).setMaxlength(30);
		pwdInput.setID("password");
		div1.addElement(pwdInput);
		fs.addElement(div1);
		
		div1 = new div();
		div1.setClass("row");

		//	Language Pick
		label langLabel = new label().setFor(Env.LANGUAGE + "F").addElement(lngText);
		langLabel.setID(Env.LANGUAGE + "L");
		div1.addElement(langLabel);
		Env.getLoginLanguages(); // to fill the s_language array on Language
		option options[] = new option[Language.getLanguageCount()];
		for (int i = 0; i < Language.getLanguageCount(); i++)
		{
			Language language = Language.getLanguage(i);
			options[i] = new option(language.getAD_Language())
				.addElement(Util.maskHTML(language.getName()));
			if (language.getAD_Language().equals(AD_Language))
				options[i].setSelected(true);
			else
				options[i].setSelected(false);
		}
		div1.addElement(new select(Env.LANGUAGE, options)
			.setID(Env.LANGUAGE + "F"));
		fs.addElement(div1);
		
		div1 = new div();
		div1.setClass("row");

		//  Remember me
		String rememberText = Msg.getMsg(AD_Language, "RememberMe");
		String rememberMe = cProp.getProperty(P_REMEMBER, "");
		label rememberLabel = new label().setFor(P_REMEMBER + "L").addElement(rememberText);
		rememberLabel.setID(P_REMEMBER+"L");
		div1.addElement(rememberLabel);
		rememberCk = new input(input.TYPE_CHECKBOX, P_REMEMBER, rememberMe).setSize(20).setMaxlength(30);
		rememberCk.setID(P_REMEMBER);
		rememberCk.addAttribute("checked", "true");
		rememberCk.setValue("true");
		rememberCk.setOnClick("checkRemember(this);");
		div1.addElement(rememberCk);
		fs.addElement(div1);
			
		div1 = new div();
		div1.setClass("row");
		
		
		/*  Store Cookie
		label cookieLabel = new label().setFor(P_STORE + "F").addElement(storeTxt);
		cookieLabel.setID(P_STORE + "L");
		div1.addElement(cookieLabel);
		String storeData = cProp.getProperty(P_STORE, "N");
		input store = new input(input.TYPE_CHECKBOX, P_STORE, "Y").setChecked(storeData.equals("Y"));
		store.setID(P_STORE + "F");
		div1.addElement(store);
		fs.addElement(div1);*/

		
		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			div1 = new div();
			div1.setClass("row");
			div1.addElement(new font(HtmlColor.red, 4).addElement(new b(errorMessage)));   //  color, size
			fs.addElement(div1);
		}
		// search default image for theme
		//StringBuffer sb = new StringBuffer();
		String zk_theme_value=MSysConfig.getValue("ZK_THEME");
		String urldef = "/webui/theme/"+zk_theme_value+"/images/login-logo.png";
		String imgdef = MSysConfig.getValue(MSysConfig.ZK_LOGO_LARGE, urldef);

		img img=new img();
		img.setSrc(imgdef);
		

		div div0 = new div();

		div0.addElement(img);
		div0.setClass("login-box-header-logo");
		
		myForm.addElement(fs);
		
		//<a class="whiteButton" type="submit" href="#">Login</a>
		//  Finish
		a button = new a("#", "OK");
		button.addAttribute("type", "submit");
		button.setClass("whiteButton");
		
		//
		myForm.addElement(button);
		
		//  Document
		MobileDoc doc = MobileDoc.createWindow(windowTitle);
		
		div div = new div();
		div.setClass("toolbar");
		
		h1 header = new h1();
		header.setID("pageTitle");
		div.addElement(header);
		a anchor = new a();
		anchor.setID("backButton");
		anchor.setClass("button");
		div.addElement(anchor);
		
		
		if (errorMessage == null || errorMessage.length()<=0 )
		{
			doc.getBody()
			
			.addElement(div)
			.addElement(div0)

			.addElement(myForm)
			.setTitle(windowTitle);

			doc.getHead().addElement(new link(imgdef,"icon","image/png"));
			doc.getHead().addElement(new script((Element)null, MobileEnv.getBaseDirectory("/js/login.js")));	
		}
		else{
			doc.getBody()
			

			.addElement(myForm);
		}
		

		

		return doc;
	}   //  createFirstPage


	/**
	 *  Create Second Page
	 *  @param request request
	 *  @param roleOptions role options
	 *  @param errorMessage error message
	 *  @return WDoc page
	 */
	private MobileDoc createSecondPage(Properties cProp, HttpServletRequest request,
		KeyNamePair[] clients, String roleData, String usr, String errorMessage)
	{
		log.info(" - " + errorMessage);
		MobileSessionCtx wsc = MobileSessionCtx.get(request);
		String windowTitle = Msg.getMsg(wsc.language, "SelectRole");
		// make option[]
		option[] clientOptions = MobileUtil.convertToOption(clients, roleData);
		
		//	Form - Get Menu
		String action = MobileEnv.getBaseDirectory("WLogin");
		form myForm = new form(action).setName("Login2");
		myForm.setID(windowTitle);
		myForm.setTitle(windowTitle);
		myForm.addAttribute("selected", "true");
		myForm.setClass("panel");
		myForm.setMethod("post");
		myForm.setTarget("_self");
		
		//	CLient Pick
		fieldset fs = new fieldset();
		div div1 = new div();
		div1.setClass("row");
		//Modified by Rob Klein 4/29/07
		label clientLabel = new label().setFor(P_CLIENT + "F").addElement(Msg.translate(wsc.language, "AD_Client_ID"));
		clientLabel.setID(P_CLIENT + "L");
		div1.addElement(clientLabel);
		select client = new select(P_CLIENT, clientOptions);
		client.setID(P_CLIENT + "F");
		client.setOnChange("loginDynUpdate(this);");        		//  sets Client & Org
		div1.addElement(client);
		fs.addElement(div1);
		Env.setContext(wsc.ctx, Env.AD_CLIENT_ID, clients[0].getKey()); //red1 

		Login login = new Login(wsc.ctx);
		//  Get Data
		KeyNamePair[] roles = null; // roles enabled for mobile access
		if (clientOptions.length > 0) {
			roles = WLogin.filterMobileRoles(login.getRoles(APP_USER, clients[0]));
		}
		//	Role Pick
		div1 = new div();
		div1.setClass("row");
		label roleLabel = new label().setFor(P_ROLE + "F").addElement(Msg.translate(wsc.language, "AD_Role_ID"));
		roleLabel.setID(P_ROLE + "L");
		div1.addElement(roleLabel);
		select role = new select(P_ROLE, MobileUtil.convertToOption(roles, null));
		role.setID(P_ROLE + "F");
		role.setOnChange("loginDynUpdate(this);");        		//  sets Org
		div1.addElement(new td().addElement(role));
		fs.addElement(div1);

		KeyNamePair[] orgs = null;
		if ( roles.length > 0 )
		{
			orgs = login.getOrgs (roles[0]);
		}
		
		//	Org Pick
		div1 = new div();
		div1.setClass("row");
		label orgLabel = new label().setFor(P_ORG + "F").addElement(Msg.translate(wsc.language, "AD_Org_ID"));
		orgLabel.setID(P_ORG + "L");
		div1.addElement(orgLabel);
		String orgData = cProp.getProperty(P_ORG, null);
		select org = new select(P_ORG, MobileUtil.convertToOption(orgs, orgData));
		org.setID(P_ORG + "F");
		div1.addElement(org);
		fs.addElement(div1);

		//  ErrorMessage
		if (errorMessage != null && errorMessage.length() > 0)
		{
			div1 = new div();
			div1.setClass("row");
			div1.addElement(new strong(errorMessage));
			fs.addElement(div1);
		}
		
		myForm.addElement(fs);
		
		//  Finish
		a button = new a("#", "OK");
		button.addAttribute("type", "submit");
		button.setClass("whiteButton");
		
		myForm.addElement(button);
		
	//  Document
		MobileDoc doc = MobileDoc.createWindow(windowTitle);
		
		doc.getBody()
		.addElement(myForm)
			.setTitle("Login");

		return doc;
	}   //  createSecondPage

	public static KeyNamePair[] filterMobileRoles(KeyNamePair[] tmproles) {
		// Only keep roles that are designed for mobile
		List<KeyNamePair> mobileRolesList = new ArrayList<KeyNamePair>();
		for (int i = 0; i < tmproles.length; i++) {
			if (new MRole(Env.getCtx(), tmproles[i].getKey(), null).get_ValueAsBoolean("IsMobileEnabled")) 
				mobileRolesList.add(tmproles[i]);
		}
		KeyNamePair[] roles = new KeyNamePair[mobileRolesList.size()];
		mobileRolesList.toArray(roles);
		return roles;
	}

    public static void setUserID(Properties ctx, int clientId) {
    	if (clientId >= 0) {
        	Env.setContext(ctx, "#AD_Client_ID", clientId);
    	} else {
        	Env.setContext(ctx, "#AD_Client_ID", (String) null);
    	}
    	MUser user = MUser.get (ctx, WLogin.APP_USER);
    	if (user != null) {
    		Env.setContext(ctx, "#AD_User_ID", user.getAD_User_ID() );
    		Env.setContext(ctx, "#AD_User_Name", user.getName() );
    		Env.setContext(ctx, "#SalesRep_ID", user.getAD_User_ID() );
    	}
    }

}	//	WLogin
