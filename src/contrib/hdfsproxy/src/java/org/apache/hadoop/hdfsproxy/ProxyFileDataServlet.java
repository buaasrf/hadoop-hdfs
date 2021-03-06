/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfsproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.server.common.JspHelper;
import org.apache.hadoop.hdfs.server.namenode.FileDataServlet;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.security.UserGroupInformation;

/** {@inheritDoc} */
public class ProxyFileDataServlet extends FileDataServlet {
  /** For java.io.Serializable */
  private static final long serialVersionUID = 1L;

  /** {@inheritDoc} */
  @Override
  protected URI createUri(String parent, HdfsFileStatus i, UserGroupInformation ugi,
      ClientProtocol nnproxy, HttpServletRequest request, String dt) throws IOException,
      URISyntaxException {
    String dtParam="";
    if (dt != null) {
      dtParam=JspHelper.getDelegationTokenUrlParam(dt);
    }
    InetSocketAddress nnAddress = (InetSocketAddress) getServletContext()
        .getAttribute(NameNode.NAMENODE_ADDRESS_ATTRIBUTE_KEY);
    String nnHostPort = nnAddress == null ? null : NameNode
        .getHostPortString(nnAddress);
    String addrParam = JspHelper.getUrlParam(JspHelper.NAMENODE_ADDRESS,
        nnHostPort);
    return new URI(request.getScheme(), null, request.getServerName(), request
        .getServerPort(), "/streamFile" + i.getFullName(parent),
        "&ugi=" + ugi.getShortUserName() + dtParam + addrParam, null);
  }

  /** {@inheritDoc} */
  @Override
  protected UserGroupInformation getUGI(HttpServletRequest request,
                                        Configuration conf) {
    String userID = (String) request
        .getAttribute("org.apache.hadoop.hdfsproxy.authorized.userID");
    return ProxyUtil.getProxyUGIFor(userID);
  }
}
