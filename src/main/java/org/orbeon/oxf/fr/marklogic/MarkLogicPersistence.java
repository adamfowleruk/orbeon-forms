/*
 * Copyright (C) 2014 MarkLogic Corporation
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.fr.marklogic;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import javax.servlet.ServletException;
import org.orbeon.oxf.util.ScalaUtils._;
import org.orbeon.saxon.value.DateTimeValue;
import java.util.Date;
import xml.NodeSeq._;
import xml.NodeSeq;
import xml.Node;
import xml.XML;
import java.util.UUID;

/**
 * A MarkLogic Persistence layer for Orbeon Forms. Works by REST endpoints. Will also need configuration in Orbeon XML.
 *
 * All form definitions held in /orbeon/fr/<app>/<form>/definition.xml in orbeon/fr,app/<app>,form/<form>,formdefinitions collections
 * All form data held in /orbeon/fr/<app>/<form>/data/<instanceid>.xml or .xhtml for html info in orbeon/fr,app/<app>,form/<form>,formdata/<instanceid>formdataxml|formdatahtml collections
 * All attachments held in /orbeon/fr/<app>/<form>/data/attachments/<instanceid>.<mimeext> in orbeon/fr,app/<app>,form/<form>,formdata/<instanceid>,attachment collections
 * All metadata held in elements within the <orbeon> element in the properties fragment
 * All searches use collections, thus for better performance enable the collections lexicon
 */
public class MarkLogicPersistence extends HttpServlet {

  protected Pattern dataPath = Pattern.compile(".*/crud/([^/]+)/([^/]+)/data/([^/]+)/([^/]+)");
  protected Pattern draftPath = Pattern.compile(".*/crud/([^/]+)/([^/]+)/draft/([^/]+)/([^/]+)");
  protected Pattern formPath = Pattern.compile(".*/crud/([^/]+)/([^/]+)/form/([^/]+)");
  protected Pattern searchPath = Pattern.compile(".*/search/([^/]+)/([^/]+)/?");

  protected DatabaseClient client = null;



  /**
   * Constructor. Initialises DB connection.
   */
  public MarkLogicPersistence() {
    // TODO db initialisation from external XML config file in webapp configuration
    client = DatabaseClientFactory.newClient("192.168.123.225",
               7202, "admin", "admin", Authentication.DIGEST);
    // TODO perhaps one connection for form definitions, another for form data?
    super();
  }







  /* Public servlet methods */

  public void doPut(HttpServletRequest req,HttpServletResponse res) throws ServletExeption {
    Matcher dataMatcher = dataPath.matcher(req.getPathInfo());
    Matcher formMatcher = formPath.matcher(req.getPathInfo());

    String app = "";
    String form = "";
    String documentId = "";
    String attachmentName = "";


    if (dataMatcher.find()) {
      // end could be data.xml
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);
      documentId = dataMatcher.group(3);
      attachmentName = dataMatcher.group(4);
      if ("data.xml" == attachmentName) {
        storeDocument(app,form,documentId,req.getInputStream());
      } else {
        storeAttachment(app,form,documentId,attachmentName,req);
      }

    } else if (formMatcher.find()) {
      // end could be form.xhtml
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);
      attachmentName = dataMatcher.group(3);
      if ("form.xhtml" == attachmentName) {
        storeForm(app,form,req.getInputStream());
      } else {
        storeFormAttachment(app,form,attachmentName,req);
      }
    } else {
      // no match - ignore
    }

  }

  public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletExeption {
    Matcher dataMatcher = dataPath.matcher(req.getPathInfo());
    Matcher draftMatcher = draftPath.matcher(req.getPathInfo());
    Matcher formMatcher = formPath.matcher(req.getPathInfo());

    String app = "";
    String form = "";
    String documentId = "";
    String attachmentName = "";


    if (dataMatcher.find()) {
      // end could be data.xml
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);
      documentId = dataMatcher.group(3);
      attachmentName = dataMatcher.group(4);
      if ("data.xml" == attachmentName) {
        retrieveDocument(app,form,documentId,false,res);
      } else {
        retrieveAttachment(app,form,documentId,false,attachmentName,res);
      }

    } else if (draftMatcher.find()) {
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);
      documentId = dataMatcher.group(3);
      attachmentName = dataMatcher.group(4);
      if ("data.xml" == attachmentName) {
        retrieveDocument(app,form,documentId,true,res);
      } else {
        retrieveAttachment(app,form,documentId,true,attachmentName,res);
      }
    } else if (formMatcher.find()) {
      // end could be form.xhtml
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);
      attachmentName = dataMatcher.group(3);
      if ("form.xhtml" == attachmentName) {
        retrieveForm(app,form,res);
      } else {
        retrieveFormAttachment(app,form,attachmentName,res);
      }
    } else {
      // no match - ignore
    }

  }

  public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletExeption {
    Matcher searchMatcher = searchPath.matcher(req.getPathInfo());

    String app = "";
    String form = "";

    if (searchMatcher.find()) {
      // end could be data.xml
      app = dataMatcher.group(1);
      form = dataMatcher.group(2);

      search(app,form,req,res);

    } else {
      // do nothing
    }

  }







  /* Implementation methods - protected so a subclass could override them (E.g. one Orbeon forms system with multiple persistence requirements on ML) */

  protected void storeDocument(String app,String form,String documentId, boolean draft,InputStream inputStream) {

  }

  protected void retrieveDocument(String app,String form,String documentId,boolean draft,HttpServletResponse res) {

  }

  protected void storeAttachment(String app,String form,String documentId,boolean draft,String name,HttpServletRequest req) {

  }

  protected void retrieveAttachment(String app,String form,String documentId,boolean draft,String name,HttpServletResponse res) {

  }

  protected void storeForm(String app,String form,InputStream inputStream) {
    //String name = UUID.randomUUID();
    // All form definitions held in /orbeon/<app>/<form>/definition.xml in orbeon,app/<app>,form/<form>,formdefinitions collections
    String formUri = "/orbeon/fr/" + app + "/" + form + "/definition.xml";
    XMLDocumentManager docMgr = client.newXMLDocumentManager();
    InputStreamHandle isr = new InputStreamHandle(inputStream);
    isr.setFormat(Format.XML);
    DocumentMetadataHandle meta = new DocumentMetadataHandle();
    meta.withCollections("orbeon/fr","/app/" + app,"/form/" + form,"formdefinitions")
        /*.withProperty(new QName("","orbeon"),
          "<created></created><updated></updated>" // need this for form data, not really form definitions interesting - really???
          // form data would include <keywords><keyword>keyword1</keyword><keyword>keyword2</keyword></keywords>
        )*/
        ;


    docMgr.write(formUri,meta,isr);
  }

  protected void retrieveForm(String app,String form,HttpServletResponse res) {
    String formUri = "/orbeon/fr/" + app + "/" + form + "/definition.xml";
    XMLDocumentManager docMgr = client.newXMLDocumentManager();
    res.setContentType("application/xhtml+xml"); // weird orbeon forms thing
    DocumentMetadataHandle meta = new DocumentMetadataHandle();
    InputStreamHandle isr = new InputStreamHandle();
    docMgr.read(formUri,meta,isr);
    // TODO something with metadata?
    isr.write(res.getOutputStream());
  }

  protected void storeFormAttachment(String app,String form,String name,HttpServletRequest req) {

  }

  protected void retrieveFormAttachment(String app,String form,String name,HttpServletResponse res) {

  }

  protected void search(String app,String form,HttpServletRequest req,HttpServletResponse res) {

  }

}
