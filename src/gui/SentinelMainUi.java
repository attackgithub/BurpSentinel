/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui;

import burp.IHttpRequestResponse;
import burp.IProxyListener;
import burp.ITab;
import burp.MainUiInterface;
import gui.mainBot.PanelBotUi;
import gui.mainTop.PanelTopUi;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import service.SentinelProxyListener;
import util.BurpCallbacks;
import util.UiUtil;

/**
 * The main GUI Window
 *
 * - Displays Top and Bottom Panel 
 * - Interface between Top/Bot Panel 
 * - Interface for burp to add HttpMessages to sentinel
 *
 * @author Dobin
 */
public class SentinelMainUi extends javax.swing.JPanel implements ITab, MainUiInterface {

    // A list of Panels of the added HttpMessages
    private LinkedList<PanelBotUi> panelBotUiList = new LinkedList<PanelBotUi>();
    
    // Current selected panel
    private PanelBotUi currentPanelBot = null;
    
    
    static void setMainUi(SentinelMainUi ui) {
        mainUi = ui;
    }
    static SentinelMainUi mainUi;
    public static SentinelMainUi getMainUi() {
        return mainUi;
    }
    
    
    /**
     * Creates new form MainGuiFrame
     */
    public SentinelMainUi() {
        initComponents();
    }

    
    public void init() {
        proxyListener = new SentinelProxyListener();
        
        SentinelMainUi.setMainUi(this);
        
        // Has to be on top-top, or will not restore split location correclty
        UiUtil.restoreSplitLocation(jSplitPane1, this);
        
        // Has to be on top, or it breaks panelTopUi init stuff
        initTestMessages();
        
        // panelTopUi was inserted with Netbeans palette
        // Set his parent here
        panelTopUi.init();
    }
    
    public PanelTopUi getPanelTop() {
        return panelTopUi;
    }

    
    public void addNewMessage(SentinelHttpMessageOrig myHttpMessage) {
        // Save ui preferences
        // For example, the row width's are not automatically stored upon change,
        // but needed for new messages.
        storeUiPrefs();

        // Add request to top overview (where user can select requests)
        panelTopUi.addMessage(myHttpMessage);

        // Create a new PanelBot card and add it to the botPanel and the 
        // LinkedList of available cards
        int index = panelBotUiList.size();
        PanelBotUi newPanelBot = new PanelBotUi(myHttpMessage);
        panelBotUiList.add(newPanelBot);
        panelCard.add(newPanelBot, Integer.toString(index));
        showMessage(index); // Show newly added message in ui
    }
    
    /* Add new HttpRequestResponse
     * This gets called from (external) Burp Menu entry
     * 
     * this is the main entry point for new HttpMessages (IHttpRequestResponse)
     */
    @Override
    public void addNewMessage(IHttpRequestResponse iHttpRequestResponse) {
        // Make a sentinel http message from the burp message
        SentinelHttpMessageOrig myHttpMessage = new SentinelHttpMessageOrig(iHttpRequestResponse);
        
        this.addNewMessage(myHttpMessage);

    }

    /*
     * Show a HttpMessage - based on it's index (derived from top overview)
     * 
     */
    public void showMessage(int index) {
        panelTopUi.setSelected(index);
        CardLayout cl = (CardLayout) panelCard.getLayout();
        cl.show(panelCard, Integer.toString(index));
        
        if (index >= panelBotUiList.size()) {
            BurpCallbacks.getInstance().print("MainUi showMessage Error: Size of list: " + panelBotUiList.size() + " Selected: " + index);
        } else {
            currentPanelBot = panelBotUiList.get(index);
        }
    }
    
    public HashMap<SentinelHttpMessage, LinkedList<SentinelHttpMessageAtk>> getAllMessageList() {
        HashMap<SentinelHttpMessage, LinkedList<SentinelHttpMessageAtk>> map;
        map = new HashMap<SentinelHttpMessage, LinkedList<SentinelHttpMessageAtk>>();
        
        for(PanelBotUi panelBot: panelBotUiList) {
            SentinelHttpMessage origHttpMessage = panelBot.getOrigMessage();
            LinkedList<SentinelHttpMessageAtk> atkHttpMessages = panelBot.getAttackMessages();
            
            map.put(origHttpMessage, atkHttpMessages);
        }
        
        return map;
    }
    
    
    public void removeMessage(int currentSelectedRow) {
        // TODO FIXME breaks if removed...
        //panelBotUiList.remove(currentSelectedRow);
        //panelCard.remove(currentSelectedRow);
    }
    
    // TODO
    public void updateBottomPanel() {
        if (currentPanelBot == null) {
            return;
        }
        currentPanelBot.externalUpdate();
    }

    
    /*
     * Init testcase messages
     * 
     */
    private void initTestMessages() {
        String a = "";
        a += "GET /vulnerable/test1.php?testparam=test%27 HTTP/1.1\r\n";
        a += "Host: www.dobin.ch\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "\r\n";
        SentinelHttpMessage httpMessage = new SentinelHttpMessageOrig(a, "www.dobin.ch", 80, false);
        addNewMessage(httpMessage);


        a = "";
        a += "GET /vulnerable/test1.php?abcdefgh HTTP/1.1\r\n";
        a += "Host: www.dobin.ch\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Cookie: jsessionid=asdfa; bbbbb=ddddd\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "\r\n";
        httpMessage = new SentinelHttpMessageOrig(a, "www.dobin.ch", 80, false);
        addNewMessage(httpMessage);


        a = "";
        a += "POST /vulnerable/test2.php HTTP/1.1\r\n";
        a += "Host: www.dobin.ch\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "Cookie: jsessionid=useraaaa; bbbbb=ddddd\r\n";
        a += "Content-Type: application/x-www-form-urlencoded\r\n";
        a += "Content-Length: 26\r\n";
        a += "\r\n";
        a += "bla=blaaa&testparam=teeest";
        httpMessage = new SentinelHttpMessageOrig(a, "www.dobin.ch", 80, false);
        addNewMessage(httpMessage);


        a = "";
        a += "GET /vulnerable/test3.php?name=test1&testparam=test2 HTTP/1.1\r\n";
        a += "Host: www.dobin.ch\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "Cookie: jsessionid=useraaaa; bbbbb=ddddd\r\n";
        a += "\r\n";
        httpMessage = new SentinelHttpMessageOrig(a, "www.dobin.ch", 80, false);
        addNewMessage(httpMessage);


        a = "";
        a += "POST http://192.168.140.134/vulnerable/testing.php?name=test1&testparam=&aaa= HTTP/1.1\r\n";
        a += "Host: 192.168.140.134\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "Cookie: jsessionid=userbbb; jive.server.info=\"serverName=as-3:serverPort=80:contextPath=:localName=localhost:localPort=9001:localAddr=127.0.0.1\"; ROUTEID=.AS-3; SPRING_SECURITY_REMEMBER_ME_COOKIE=YzEwMDAwMDoxMzU3MDI5NzM1Njk2OjQxZGJkNGRiODZhNWZlNjU4OWQ4YjEyYWM0Y2QyZDVi; jive.user.loggedIn=true\r\n";
        a += "\r\n";
        a += "lll1=aaa1\r\n";
        a += "lll2=aaa2\r\n";
        httpMessage = new SentinelHttpMessageOrig(a, "192.168.140.134", 80, false);
        addNewMessage(httpMessage);


        a = "";
        a += "POST http://192.168.140.134/vulnerable/testing.php?name=test1&testparam=&aaa= HTTP/1.1\r\n";
        a += "Host: 192.168.140.134\r\n";
        a += "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0\r\n";
        a += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        a += "Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3\r\n";
        a += "Accept-Encoding: gzip, deflate\r\n";
        a += "Proxy-Connection: keep-alive\r\n";
        a += "\r\n";
        a += "Content-Type: multipart/form-data; boundary=---------------------------1645864822313206347576655232\r\n";
        a += "Content-Length: 3153\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";
        a += "Content-Disposition: form-data; name=\"utf8\"\r\n";
        a += "\r\n";
        a += "aaa\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";
        a += "Content-Disposition: form-data; name=\"_method\"\r\n";
        a += "\r\n";
        a += "put\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";
        a += "Content-Disposition: form-data; name=\"authenticity_token\"\r\n";
        a += "\r\n";
        a += "PTNmG3crwtME0kRijri1uNfS6b8l9ET2CLvZydnEhD4=\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";
        a += "Content-Disposition: form-data; name=\"dossier[title]\"\r\n";
        a += "\r\n";
        a += "\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";
        a += "Content-Disposition: form-data; name=\"dossier[prename]\"\r\n";
        a += "\r\n";
        a += "Snb5\r\n";
        a += "-----------------------------1645864822313206347576655232\r\n";

        httpMessage = new SentinelHttpMessageOrig(a, "192.168.140.134", 80, false);
        addNewMessage(httpMessage);
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        panelTop = new javax.swing.JPanel();
        panelTopUi = new gui.mainTop.PanelTopUi();
        panelBot = new javax.swing.JPanel();
        panelCard = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        panelTop.setLayout(new java.awt.BorderLayout());
        panelTop.add(panelTopUi, java.awt.BorderLayout.CENTER);

        jSplitPane1.setTopComponent(panelTop);

        panelBot.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelBot.setLayout(new java.awt.BorderLayout());

        panelCard.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelCard.setLayout(new java.awt.CardLayout());
        panelBot.add(panelCard, java.awt.BorderLayout.CENTER);

        jSplitPane1.setBottomComponent(panelBot);

        jMenu1.setText("File");

        jMenuItem2.setText("Load Tests");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem1.setText("Reset");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1214, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //this.dispose();
        //initComponents();
        //init();
        panelTopUi.reset();
        panelBotUiList = new LinkedList<PanelBotUi>();
        panelCard.removeAll();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        initTestMessages();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel panelBot;
    private javax.swing.JPanel panelCard;
    private javax.swing.JPanel panelTop;
    private gui.mainTop.PanelTopUi panelTopUi;
    // End of variables declaration//GEN-END:variables

    
    public void storeUiPrefs() {
        // store this preferences
        UiUtil.storeSplitLocation(jSplitPane1, this);
        
        panelTopUi.storeUiPrefs();

        // Store table preferences of last PanelBottom
        if (panelBotUiList.size() != 0) {
            panelBotUiList.get(panelBotUiList.size() - 1).storeUiPrefs();
        } else {
        }
    }
    
    
    @Override
    public String getTabCaption() {
        return "Sentinel";
    }
    
    @Override
    public Component getUiComponent() {
        return this;
    }
    
    private SentinelProxyListener proxyListener;

    public IProxyListener getProxyListener() {
        return proxyListener;
    }

}
