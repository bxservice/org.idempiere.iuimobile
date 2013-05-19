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
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compiere.util.CLogger;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Login;

/**
 *  Dynamic Field Updates.
 * 	
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: WFieldUpdate.java,v 1.1 2009/04/15 11:27:15 vinhpt Exp $
 */
public class LoginDynUpdate extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1576213475379404148L;
	
	/**	Static Logger	*/
	private static CLogger	log	= CLogger.getCLogger (LoginDynUpdate.class);
	
	/**
	 *  Initialize global variables
	 *  @param config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!MobileEnv.initWeb(config))
			throw new ServletException("WFieldUpdate.init");
	}   //  init

	/**
	 *  Clean up resources
	 */
	public void destroy()
	{
	}   //  destroy

	 /**
	 *  Process the HTTP Get request
	 *  @param request
	 *  @param response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}   //  doPost


	/**
	 *  Process the HTTP Post request
	 *  @param request
	 *  @param response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException
	{
		//  Get Session Info
		MobileSessionCtx wsc = MobileSessionCtx.get(request);
		WWindowStatus ws = WWindowStatus.get(request);

		if (wsc == null || ws == null)    //  ws can be null for Login
			;
		String usr = WLogin.APP_USER;
		String client = request.getParameter("AD_Client_ID");
		int clientId;
		try {
			clientId = Integer.parseInt(client); 
		}
		catch (Exception e) {
			clientId = -1;
		}
		String role = request.getParameter("AD_Role_ID");
		int roleId;
		try {
			roleId = Integer.parseInt(role); 
		}
		catch (Exception e) {
			roleId = -1;
		}
		Login login = new Login(wsc.ctx);
		StringBuffer script = new StringBuffer ("{");
		
		if (clientId >= 0 )
		{
			//  Get Data
			KeyNamePair[] roles = login.getRoles (usr,
					new KeyNamePair(clientId , client));

			//  Set Client ----
			script.append("\"roles\":[");
			for (int i = 0; i < roles.length; i++)
			{
				if ( i > 0 )
					script.append(",");
				KeyNamePair p = roles[i];
				script.append("{\"text\":\"");
				script.append(p.getName());     //  text
				script.append("\",\"value\":\"");
				script.append(p.getKey());      //  value
				script.append("\"}");
			}
			script.append("]");
			
			if ( roleId < 0 && roles.length > 0 )
			{
				roleId = roles[0].getKey();
			}
		}
		
		if ( roleId >= 0 )
		{
			script.append(",\"orgs\":[");

			KeyNamePair[] orgs = login.getOrgs (new KeyNamePair(roleId, role));
			for (int i = 0; i < orgs.length; i++)
			{
				if ( i > 0 )
					script.append(",");
				KeyNamePair p = orgs[i];
				script.append("{\"text\":\"");
				script.append(p.getName());     //  text
				script.append("\",\"value\":\"");
				script.append(p.getKey());      //  value
				script.append("\"}");
			}
			script.append("]");
		}
		script.append("}");
	//  print document
		PrintWriter out = response.getWriter();     //  with character encoding support
		out.print(script);
		out.flush();
		if (out.checkError())
			log.log(Level.SEVERE, "error writing");
		//  binary output (is faster but does not do character set conversion)
	//	OutputStream out = response.getOutputStream();
	//	byte[] data = doc.toString().getBytes();
	//	response.setContentLength(data.length);
	//	out.write(doc.toString().getBytes());
		//
		out.close();
	}   //  doPost

}   //  WFieldUpdate
