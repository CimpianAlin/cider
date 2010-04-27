/**
 *  URI.java
 *  Copyright 2010 by Michael Peter Christen
 *  First released 22.4.2010 at http://yacy.net
 *  
 *  This file is part of YaCy Content Integration
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file COPYING.LESSER.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.cider.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import net.yacy.cider.util.FileUtils;
import net.yacy.cider.util.Log;
import net.yacy.cider.util.Punycode;
import net.yacy.cider.util.Punycode.PunycodeException;

public class URI {
    
    private static final Logger thislog = Logger.getLogger(URI.class.getName());

    private static final Pattern BACK_PATH = Pattern.compile("(/[^/]+(?<!/\\.{1,2})/)[.]{2}(?=/|$)|/\\.(?=/)|/(?=/)");
    private static final Pattern DOT       = Pattern.compile("\\.");
    private static final Pattern SLASH     = Pattern.compile("/");
    private static final Pattern AMP       = Pattern.compile("&");
    private static final Pattern MAILCHARS = Pattern.compile("^[a-z]+:.*?");
    
    // session id handling
    private static final Collator insensitiveCollator = Collator.getInstance(Locale.US);
    private static final TreeSet<String> sessionIDnames;
    static {
        insensitiveCollator.setStrength(Collator.SECONDARY);
        insensitiveCollator.setDecomposition(Collator.NO_DECOMPOSITION);
        sessionIDnames = new TreeSet<String>(insensitiveCollator);
    }
    
    public static final void initSessionIDNames(File idNamesFile) {
        for (String s: FileUtils.loadList(idNamesFile)) {
            if (s == null) continue;
            s = s.trim();
            if (s.length() > 0) sessionIDnames.add(s);
        }
    }
    
    // class variables
    private String protocol, host, userInfo, path, quest, ref;
    private int port;
    
    public URI(final File file) throws MalformedURLException {
        this("file", "", -1, file.getAbsolutePath());
    }
    
    public URI(final String url) throws MalformedURLException {
        if (url == null) throw new MalformedURLException("url string is null");
        
        parseURLString(url);
    }
    
    public static final boolean isHTTP(String s) { return s.startsWith("http://"); }
    public static final boolean isHTTPS(String s) { return s.startsWith("https://"); }
    public static final boolean isFTP(String s) { return s.startsWith("ftp://"); }
    public static final boolean isFile(String s) { return s.startsWith("file://"); }
    public static final boolean isSMB(String s) { return s.startsWith("smb://") || s.startsWith("\\\\"); }

    public final boolean isHTTP()  { return this.protocol.equals("http"); }
    public final boolean isHTTPS() { return this.protocol.equals("https"); }
    public final boolean isFTP()   { return this.protocol.equals("ftp"); }
    public final boolean isFile()  { return this.protocol.equals("file"); }
    public final boolean isSMB()   { return this.protocol.equals("smb"); }

    private void parseURLString(String url) throws MalformedURLException {
        // identify protocol
        assert (url != null);
        url = url.trim();
        if (url.startsWith("\\\\")) {
            url = "smb://" + url.substring(2).replaceAll("\\\\", "/");
        }
        
        if (url.length() > 1 && url.charAt(1) == ':') {
            // maybe a DOS drive path
            url = "file://" + url;
        }
        
        if (url.length() > 0 && url.charAt(0) == '/') {
            // maybe a unix/linux absolute path
            url = "file://" + url;
        }
        
        int p = url.indexOf(':');
        if (p < 0) {
            url = "http://" + url;
            p = 4;
        }
        this.protocol = url.substring(0, p).toLowerCase().trim();
        if (url.length() < p + 4) throw new MalformedURLException("URL not parseable: '" + url + "'");
        if (!this.protocol.equals("file") && url.substring(p + 1, p + 3).equals("//")) {
            // identify host, userInfo and file for http and ftp protocol
            final int q = url.indexOf('/', p + 3);
            int r;
            if (q < 0) {
                if ((r = url.indexOf('@', p + 3)) < 0) {
                    host = url.substring(p + 3);
                    userInfo = null;
                } else {
                    host = url.substring(r + 1);
                    userInfo = url.substring(p + 3, r);
                }
                path = "/";
            } else {
                host = url.substring(p + 3, q).trim();
                if ((r = host.indexOf('@')) < 0) {
                    userInfo = null;
                } else {
                    userInfo = host.substring(0, r);
                    host = host.substring(r + 1);
                }
                path = url.substring(q);
            }
            if (host.length() < 4 && !protocol.equals("file")) throw new MalformedURLException("host too short: '" + host + "'");
            if (host.indexOf('&') >= 0) throw new MalformedURLException("invalid '&' in host");
            path = resolveBackpath(path);
            identPort(url, (isHTTP() ? 80 : (isHTTPS() ? 443 : (isFTP() ? 21 : (isSMB() ? 445 : -1)))));
            identRef();
            identQuest();
            escape();
        } else {
            // this is not a http or ftp url
            if (protocol.equals("mailto")) {
                // parse email url
                final int q = url.indexOf('@', p + 3);
                if (q < 0) {
                    throw new MalformedURLException("wrong email address: " + url);
                }
                userInfo = url.substring(p + 1, q);
                host = url.substring(q + 1);
                path = null;
                port = -1;
                quest = null;
                ref = null;
            } if (protocol.equals("file")) {
                // parse file url
                String h = url.substring(p + 1);
                if (h.startsWith("//")) {
                    // host may be given, but may be also empty
                    final int q = h.indexOf('/', 2);
                    if (q <= 0) {
                        // no host given
                        host = null;
                        path = h.substring(2);
                    } else {
                        host = h.substring(2, q);
                        if (host.length() == 0 || host.equals("localhost")) host = null;
                        h = h.substring(q);
                        char c = h.charAt(2);
                        if (c == ':' || c == '|')
                            path = h.substring(1);
                        else
                            path = h;
                    }
                } else {
                    host = null;
                    if (h.length() > 0 && h.charAt(0) == '/') {
                        char c = h.charAt(2);
                        if (c == ':' || c == '|')
                            path = h.substring(1);
                        else
                            path = h;
                    } else {
                        char c = h.charAt(1);
                        if (c == ':' || c == '|')
                            path = h;
                        else
                            path = "/" + h;
                    }
                }
                userInfo = null;
                port = -1;
                quest = null;
                ref = null;
            } else {
                throw new MalformedURLException("unknown protocol: " + url);
            }
        }
        
        // handle international domains
        if (!Punycode.isBasic(host)) try {
            final String[] domainParts = DOT.split(host, 0);
            StringBuilder buffer = new StringBuilder();
            // encode each domain-part separately
            for(int i=0; i<domainParts.length; i++) {
            final String part = domainParts[i];
            if(!Punycode.isBasic(part)) {
                buffer.append("xn--" + Punycode.encode(part));
            } else {
                buffer.append(part);
            }
            if(i != domainParts.length-1) {
                buffer.append('.');
            }
            }
            host = buffer.toString();
        } catch (final PunycodeException e) {}
    }

    public static URI newURL(final String baseURL, final String relPath) throws MalformedURLException {
        if ((baseURL == null) ||
            isHTTP(relPath) ||
            isHTTPS(relPath) ||
            isFTP(relPath) ||
            isFile(relPath) ||
            isSMB(relPath)/*||
            relPath.contains(":") && patternMail.matcher(relPath.toLowerCase()).find()*/) {
            return new URI(relPath);
        }
        return new URI(new URI(baseURL), relPath);
    }
    
    public static URI newURL(final URI baseURL, final String relPath) throws MalformedURLException {
        if ((baseURL == null) ||
            isHTTP(relPath) ||
            isHTTPS(relPath) ||
            isFTP(relPath) ||
            isFile(relPath) ||
            isSMB(relPath)/*||
            relPath.contains(":") && patternMail.matcher(relPath.toLowerCase()).find()*/) {
            return new URI(relPath);
        }
        return new URI(baseURL, relPath);
    }
    
    public URI(final URI baseURL, String relPath) throws MalformedURLException {
        if (baseURL == null) throw new MalformedURLException("base URL is null");
        if (relPath == null) throw new MalformedURLException("relPath is null");

        this.protocol = baseURL.protocol;
        this.host = baseURL.host;
        this.port = baseURL.port;
        this.userInfo = baseURL.userInfo;
        if (relPath.startsWith("//")) {
            // a "network-path reference" as defined in rfc2396 denotes
            // a relative path that uses the protocol from the base url
            relPath = baseURL.protocol + ":" + relPath;
        }
        if (relPath.toLowerCase().startsWith("javascript:")) {
            this.path = baseURL.path;
        } else if (
                isHTTP(relPath) ||
                isHTTPS(relPath) ||
                isFTP(relPath) ||
                isFile(relPath) ||
                isSMB(relPath)) {
            this.path = baseURL.path;
        } else if (relPath.contains(":") && MAILCHARS.matcher(relPath.toLowerCase()).find()) { // discards also any unknown protocol from previous if
            throw new MalformedURLException("relative path malformed: " + relPath);
        } else if (relPath.length() > 0 && relPath.charAt(0) == '/') {
            this.path = relPath;
        } else if (baseURL.path.endsWith("/")) {
            if (relPath.length() > 0 && (relPath.charAt(0) == '#' || relPath.charAt(0) == '?')) {
                throw new MalformedURLException("relative path malformed: " + relPath);
            }
            this.path = baseURL.path + relPath;
        } else {
            if (relPath.length() > 0 && (relPath.charAt(0) == '#' || relPath.charAt(0) == '?')) {
                this.path = baseURL.path + relPath;
            } else {
                final int q = baseURL.path.lastIndexOf('/');
                if (q < 0) {
                    this.path = relPath;
                } else {
                    this.path = baseURL.path.substring(0, q + 1) + relPath;
                }
            }
        }
        this.quest = baseURL.quest;
        this.ref = baseURL.ref;

        path = resolveBackpath(path);
        identRef();
        identQuest();
        escape();
    }
    
    public URI(final String protocol, final String host, final int port, final String path) throws MalformedURLException {
        if (protocol == null) throw new MalformedURLException("protocol is null");
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        identRef();
        identQuest();
        escape();
    }

    public String resolveBackpath(String p) {
        
        if (p.length() == 0 || p.charAt(0) != '/') { p = "/" + p; }

        final Matcher matcher = BACK_PATH.matcher(p);
        while (matcher.find()) {
            p = matcher.replaceAll("");
            matcher.reset(p);
        }
        
        return p.length() == 0 ? "/" : p;
    }
    
    /**
     * Escapes the following parts of the url, this object already contains:
     * <ul>
     * <li>path: see {@link #escape(String)}</li>
     * <li>ref: same as above</li>
     * <li>quest: same as above without the ampersand ("&amp;") and the equals symbol</li>
     * </ul>
     */
    private void escape() {
        if (path != null && path.indexOf('%') == -1) escapePath();
        if (quest != null && quest.indexOf('%') == -1) escapeQuest();
        if (ref != null && ref.indexOf('%') == -1) escapeRef();
    }
    
    private void escapePath() {
        final String[] pathp = SLASH.split(path, -1);
        StringBuilder ptmp = new StringBuilder(path.length() + 10);
        for (int i = 0; i < pathp.length; i++) {
            ptmp.append('/');
            ptmp.append(escape(pathp[i]));
        }
        path = ptmp.substring((ptmp.length() > 0) ? 1 : 0);
    }
    
    private void escapeRef() {
        ref = escape(ref).toString();
    }
    
    private void escapeQuest() {
        final String[] questp = AMP.split(quest, -1);
        StringBuilder qtmp = new StringBuilder(quest.length() + 10);
        for (int i = 0; i < questp.length; i++) {
            if (questp[i].indexOf('=') != -1) {
                qtmp.append('&');
                qtmp.append(escape(questp[i].substring(0, questp[i].indexOf('='))));
                qtmp.append('=');
                qtmp.append(escape(questp[i].substring(questp[i].indexOf('=') + 1)));
            } else {
                qtmp.append('&');
                qtmp.append(escape(questp[i]));
            }
        }
        quest = qtmp.substring((qtmp.length() > 0) ? 1 : 0);
    }
    
    private final static String[] hex = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
        "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
        "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
        "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
        "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
        "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
        "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
        "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
        "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
        "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
        "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
        "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
        "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
        "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
        "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
        "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
        "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
        "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
        "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
    };
    
    /**
     * Encode a string to the "x-www-form-urlencoded" form, enhanced
     * with the UTF-8-in-URL proposal. This is what happens:
     *
     * <ul>
     * <li>The ASCII characters 'a' through 'z', 'A' through 'Z',
     *     and '0' through '9' remain the same.
     *
     * <li>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
     *
     * <li>All other ASCII characters are converted into the
     *     3-character string "%xy", where xy is
     *     the two-digit hexadecimal representation of the character
     *     code
     *
     * <li>All non-ASCII characters are encoded in two steps: first
     *     to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
     *     secondly each of these bytes is encoded as "%xx".
     * </ul>
     *
     * @param s The string to be encoded
     * @return The encoded string
     */
    // from: http://www.w3.org/International/URLUTF8Encoder.java
    public static StringBuilder escape(final String s) {
        final int len = s.length();
        final StringBuilder sbuf = new StringBuilder(len + 10);
        for (int i = 0; i < len; i++) {
            final int ch = s.charAt(i);
            if ('A' <= ch && ch <= 'Z') {           // 'A'..'Z'
                sbuf.append((char)ch);
            } else if ('a' <= ch && ch <= 'z') {    // 'a'..'z'
                sbuf.append((char)ch);
            } else if ('0' <= ch && ch <= '9') {    // '0'..'9'
                sbuf.append((char)ch);
            } else if (ch == ' ') {                 // space
                sbuf.append("%20");
            } else if (ch == '&' || ch == ':'       // unreserved
                    || ch == '-' || ch == '_'
                    || ch == '.' || ch == '!'
                    || ch == '~' || ch == '*'
                    || ch == '\'' || ch == '('
                    || ch == ')' || ch == ';') {
                sbuf.append((char)ch);
            } else if (ch == '/') {                 // reserved, but may appear in post part where it should not be replaced
                sbuf.append((char)ch);
            } else if (ch <= 0x007f) {              // other ASCII
                sbuf.append(hex[ch]);
            } else if (ch <= 0x07FF) {              // non-ASCII <= 0x7FF
                sbuf.append(hex[0xc0 | (ch >> 6)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            } else {                                // 0x7FF < ch <= 0xFFFF
                sbuf.append(hex[0xe0 | (ch >> 12)]);
                sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            }
        }
        return sbuf;
    }

    // from: http://www.w3.org/International/unescape.java
    public static String unescape(final String s) {
        final int l  = s.length();
        final StringBuilder sbuf = new StringBuilder(l);
        int ch = -1;
        int b, sumb = 0;
        for (int i = 0, more = -1; i < l; i++) {
            /* Get next byte b from URL segment s */
            switch (ch = s.charAt(i)) {
                case '%':
                    if (i + 2 < l) {
                        ch = s.charAt(++i);
                        int hb = (Character.isDigit ((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                        ch = s.charAt(++i);
                        int lb = (Character.isDigit ((char) ch) ? ch - '0' : 10 + Character.toLowerCase ((char) ch) - 'a') & 0xF;
                        b = (hb << 4) | lb;
                    } else {
                        b = ch;
                    }
                    break;
                case '+':
                    b = ' ';
                    break;
                default:
                    b = ch;
            }
            /* Decode byte b as UTF-8, sumb collects incomplete chars */
            if ((b & 0xc0) == 0x80) {               // 10xxxxxx (continuation byte)
                sumb = (sumb << 6) | (b & 0x3f);    // Add 6 bits to sumb
                if (--more == 0) sbuf.append((char) sumb); // Add char to sbuf
            } else if ((b & 0x80) == 0x00) {        // 0xxxxxxx (yields 7 bits)
                sbuf.append((char) b);              // Store in sbuf
            } else if ((b & 0xe0) == 0xc0) {        // 110xxxxx (yields 5 bits)
                sumb = b & 0x1f;
                more = 1;                           // Expect 1 more byte
            } else if ((b & 0xf0) == 0xe0) {        // 1110xxxx (yields 4 bits)
                sumb = b & 0x0f;
                more = 2;                           // Expect 2 more bytes
            } else if ((b & 0xf8) == 0xf0) {        // 11110xxx (yields 3 bits)
                sumb = b & 0x07;
                more = 3;                           // Expect 3 more bytes
            } else if ((b & 0xfc) == 0xf8) {        // 111110xx (yields 2 bits)
                sumb = b & 0x03;
                more = 4;                           // Expect 4 more bytes
            } else /*if ((b & 0xfe) == 0xfc)*/ {    // 1111110x (yields 1 bit)
                sumb = b & 0x01;
                more = 5;                           // Expect 5 more bytes
            }
            /* We don't test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString();
    }

    private void identPort(final String inputURL, final int dflt) throws MalformedURLException {
        // identify ref in file
        final int r = this.host.indexOf(':');
        if (r < 0) {
            this.port = dflt;
        } else {            
            try {
                final String portStr = this.host.substring(r + 1);
                if (portStr.trim().length() > 0) this.port = Integer.parseInt(portStr);
                else this.port =  -1;               
                this.host = this.host.substring(0, r);
            } catch (final NumberFormatException e) {
                throw new MalformedURLException("wrong port in host fragment '" + this.host + "' of input url '" + inputURL + "'");
            }
        }
    }
    
    private void identRef() {
        // identify ref in file
        final int r = path.indexOf('#');
        if (r < 0) {
            this.ref = null;
        } else {
            this.ref = path.substring(r + 1);
            this.path = path.substring(0, r);
        }
    }
    
    private void identQuest() {
        // identify quest in file
        final int r = path.indexOf('?');
        if (r < 0) {
            this.quest = null;
        } else {
            this.quest = path.substring(r + 1);
            this.path = path.substring(0, r);
        }
    }
    
    public String getFile() {
        return getFile(false, false);
    }
    
    public String getFile(final boolean excludeReference, final boolean removeSessionID) {
        // this is the path plus quest plus ref
        // if there is no quest and no ref the result is identical to getPath
        // this is defined according to http://java.sun.com/j2se/1.4.2/docs/api/java/net/URL.html#getFile()
        if (quest == null) return (excludeReference || ref == null) ? path : path + "#" + ref;
        String q = quest;
        if (removeSessionID) {
            for (String sid: sessionIDnames) {
                if (q.toLowerCase().startsWith(sid.toLowerCase() + "=")) {
                    int p = q.indexOf('&');
                    if (p < 0) return (excludeReference || ref == null) ? path : path + "#" + ref;
                    q = q.substring(p + 1);
                    continue;
                }
                int p = q.toLowerCase().indexOf("&" + sid.toLowerCase() + "=");
                if (p < 0) continue;
                int p1 = q.indexOf('&', p);
                if (p1 < 0) {
                    q = q.substring(0, p);
                } else {
                    q = q.substring(0, p) + q.substring(p1);
                }
            }
        }
        return (excludeReference || ref == null) ? path + "?" + q : path + "?" + q + "#" + ref;
    }
    
    public String getFileName() {
        // this is a method not defined in any sun api
        // it returns the last portion of a path without any reference
        final int p = path.lastIndexOf('/');
        if (p < 0) return path;
        if (p == path.length() - 1) return ""; // no file name, this is a path to a directory
        return path.substring(p + 1); // the 'real' file name
    }

    public Extension getFileExtension() {
        String name = getFileName();
        int p = name.lastIndexOf('.');
        if (p < 0) return null;
        return Extension.getFileNameExtension(name.substring(p + 1));
    }
    
    public String getPath() {
        return path;
    }

    /**
     * return the file object to a local file
     * this patches also 'strange' windows file paths
     * @return the file as absolute path
     */
    public File getLocalFile() {
        char c = path.charAt(1);
        if (c == ':') return new File(path.replace('/', '\\'));
        if (c == '|') return new File(path.charAt(0) + ":" + path.substring(2).replace('/', '\\'));
        c = path.charAt(2);
        if (c == ':' || c == '|') return new File(path.charAt(1) + ":" + path.substring(3).replace('/', '\\'));
        return new File(path);
    }

    public String getAuthority() {
        return ((port >= 0) && (host != null)) ? host + ":" + port : ((host != null) ? host : "");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRef() {
        return ref;
    }

    public void removeRef() {
        ref = null;
    }
    
    public String getUserInfo() {
        return userInfo;
    }

    public String getQuery() {
        return quest;
    }

    @Override
    public String toString() {
        return toNormalform(false, true);
    }
    
    public String toNormalform(final boolean excludeReference, final boolean stripAmp) {
        return toNormalform(excludeReference, stripAmp, false);
    }
    
    public String toNormalform(final boolean excludeReference, final boolean stripAmp, final boolean removeSessionID) {
        String result = toNormalform0(excludeReference, removeSessionID); 
        if (stripAmp) {
            result = result.replaceAll("&amp;", "&");
        }
        return result;
    }
    
    private String toNormalform0(final boolean excludeReference, final boolean removeSessionID) {
        // generates a normal form of the URL
        boolean defaultPort = false;
        if (this.protocol.equals("mailto")) {
            return this.protocol + ":" + this.userInfo + "@" + this.host;
        } else if (isHTTP()) {
            if (this.port < 0 || this.port == 80)  { defaultPort = true; }
        } else if (isHTTPS()) {
            if (this.port < 0 || this.port == 443) { defaultPort = true; }
        } else if (isFTP()) {
            if (this.port < 0 || this.port == 21)  { defaultPort = true; }
        } else if (isSMB()) {
            if (this.port < 0 || this.port == 445)  { defaultPort = true; }
        } else if (isFile()) {
            defaultPort = true;
        }
        final String path = this.getFile(excludeReference, removeSessionID);
        
        if (defaultPort) {
            return
              this.protocol + "://" +
              ((this.getHost() == null) ? "" : ((this.userInfo != null) ? (this.userInfo + "@") : ("")) + this.getHost().toLowerCase()) +
              path;
        }
        return this.protocol + "://" +
               ((this.userInfo != null) ? (this.userInfo + "@") : ("")) +
               this.getHost().toLowerCase() + ((defaultPort) ? ("") : (":" + this.port)) + path;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof URI)) return false;
        URI other = (URI) obj;
        return this.toString().equals(other.toString());
    }

    public int compareTo(final Object h) {
        assert (h instanceof URI);
        return this.toString().compareTo(((URI) h).toString());
    }
    
    public boolean isPOST() {
        return (this.quest != null) && (this.quest.length() > 0);
    }

    public final boolean isCGI() {
        final String ls = unescape(path.toLowerCase());
        return ls.indexOf(".cgi") >= 0 ||
               ls.indexOf(".exe") >= 0;
    }
    
    public final boolean isIndividual() {
        final String q = unescape(path.toLowerCase());
        for (String sid: sessionIDnames) {
            if (q.startsWith(sid.toLowerCase() + "=")) return true;
            int p = q.indexOf("&" + sid.toLowerCase() + "=");
            if (p >= 0) return true;
        }
        int pos;
        return
               ((pos = q.indexOf("sid")) > 0 &&
                (q.charAt(--pos) == '?' || q.charAt(pos) == '&' || q.charAt(pos) == ';') &&
                (pos += 5) < q.length() &&
                (q.charAt(pos) != '&' && q.charAt(--pos) == '=')
                ) ||

               ((pos = q.indexOf("sessionid")) > 0 &&
                (pos += 10) < q.length() &&
                (q.charAt(pos) != '&' &&
                 (q.charAt(--pos) == '=' || q.charAt(pos) == '/'))
                ) ||

               ((pos = q.indexOf("phpsessid")) > 0 &&
                (pos += 10) < q.length() &&
                (q.charAt(pos) != '&' &&
                 (q.charAt(--pos) == '=' || q.charAt(pos) == '/')));
    }
    

    // checks for local/global IP range and local IP
    public final boolean isLocal() {
        return (this.host.startsWith("127.") || this.host.equals("localhost") || this.host.startsWith("0:0:0:0:0:0:0:1"));
    }
    

    // The URI may be used to integrate File- and SMB accessed into one object
    // some extraction methods that generate File/SmbFile objects from the URI
    
    /**
     * create a standard java URL.
     * Please call isHTTP(), isHTTPS() and isFTP() before using this class
     */
    public java.net.URL getURL() {
        if (!(isHTTP() || isHTTPS() || isFTP())) throw new UnsupportedOperationException();
        try {
            return new java.net.URL(this.toNormalform(false, true));
        } catch (MalformedURLException e) {
            // this should never happen because this class returns proper url objects
            Log.logException(thislog, e);
            return null;
        }
    }
    
    /**
     * create a standard java File.
     * Please call isFile() before using this class
     */
    public java.io.File getFSFile() {
        if (!isFile()) throw new UnsupportedOperationException();
        return new java.io.File(this.toNormalform(false, true).substring(7));
    }
    
    /**
     * create a smb File
     * Please call isSMB() before using this class
     * @throws MalformedURLException 
     */
    public SmbFile getSmbFile() throws MalformedURLException {
        if (!isSMB()) throw new UnsupportedOperationException();
        return new SmbFile(this.toNormalform(false, true));
    }
    
    // some methods that let the URI look like a java.io.File object
    // to use these methods the object must be either of type isFile() or isSMB() 
    
    public boolean exists() {
        if (isFile()) return getFSFile().exists();
        if (isSMB()) try {
            return getSmbFile().exists();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.exists SmbException for " + this.toString() + ": " + e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.exists MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public boolean canRead() {
        if (isFile()) return getFSFile().canRead();
        if (isSMB()) try {
            return getSmbFile().canRead();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.canRead SmbException for " + this.toString() + ": " + e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.canRead MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public boolean canWrite() {
        if (isFile()) return getFSFile().canWrite();
        if (isSMB()) try {
            return getSmbFile().canWrite();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.canWrite SmbException for " + this.toString() + ": " + e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.canWrite MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public boolean canExecute() {
        if (isFile()) return getFSFile().canExecute();
        if (isSMB()) return false; // no execute over smb
        return false;
    }
    
    public boolean isHidden() {
        if (isFile()) return getFSFile().isHidden();
        if (isSMB()) try {
            return getSmbFile().isHidden();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.isHidden SmbException for " + this.toString() + ": " + e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.isHidden MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public boolean isDirectory() {
        if (isFile()) return getFSFile().isDirectory();
        if (isSMB()) try {
            return getSmbFile().isDirectory();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.isDirectory SmbException for " + this.toString() + ": " + e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.isDirectory MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public long length() {
        if (isFile()) return getFSFile().length();
        if (isSMB()) try {
            return getSmbFile().length();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.length SmbException for " + this.toString() + ": " + e.getMessage());
            return 0;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.length MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return 0;
        }
        return 0;
    }
    
    public long lastModified() {
        if (isFile()) return getFSFile().lastModified();
        if (isSMB()) try {
            return getSmbFile().lastModified();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.lastModified SmbException for " + this.toString() + ": " + e.getMessage());
            return 0;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.lastModified MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return 0;
        }
        return 0;
    }
    
    public String getName() {
        if (isFile()) return getFSFile().getName();
        if (isSMB()) try {
            return getSmbFile().getName();
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.getName MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return null;
        }
        return null;
    }
    
    public String[] list() {
        if (isFile()) return getFSFile().list();
        if (isSMB()) try {
            return getSmbFile().list();
        } catch (SmbException e) {
            Log.logWarning(thislog, "SMB.list SmbException for " + this.toString() + ": " + e.getMessage());
            return null;
        } catch (MalformedURLException e) {
            Log.logWarning(thislog, "SMB.list MalformedURLException for " + this.toString() + ": " + e.getMessage());
            return null;
        }
        return null;
    }
    
    public InputStream getInputStream() throws IOException {
        if (isFile()) return new MultiProtocolInputStream(getFSFile());
        if (isSMB()) return new MultiProtocolInputStream(getSmbFile());
        return null;
    }
    
    public class MultiProtocolInputStream extends InputStream {
        private InputStream is;
        
        public MultiProtocolInputStream(File jf) throws IOException {
            this.is = new FileInputStream(jf);
        }
        
        public MultiProtocolInputStream(SmbFile sf) throws IOException {
            try {
                this.is = new SmbFileInputStream(sf);
            } catch (SmbException e) {
                throw new IOException(e);
            } catch (MalformedURLException e) {
                throw new IOException(e);
            } catch (UnknownHostException e) {
                throw new IOException(e);
            }
        }
        
        @Override
        public int read() throws IOException {
            return this.is.read();
        }

    }
    
    //---------------------
    
    private static final String splitrex = " |/|\\(|\\)|-|\\:|_|\\.|,|\\?|!|'|" + '"';
    public static final Pattern splitpattern = Pattern.compile(splitrex);
    public static String[] urlComps(String normalizedURL) {
        final int p = normalizedURL.indexOf("//");
        if (p > 0) normalizedURL = normalizedURL.substring(p + 2);
        return splitpattern.split(normalizedURL.toLowerCase()); // word components of the url
    }

    public static void main(final String[] args) {
        final String[][] test = new String[][]{
          new String[]{null, "C:WINDOWS\\CMD0.EXE"},
          new String[]{null, "file://C:WINDOWS\\CMD0.EXE"},
          new String[]{null, "file:/bin/yacy1"}, // file://<host>/<path> may have many '/' if the host is omitted and the path starts with '/'
          new String[]{null, "file:///bin/yacy2"}, // file://<host>/<path> may have many '/' if the host is omitted and the path starts with '/'
          new String[]{null, "file:C:WINDOWS\\CMD.EXE"},
          new String[]{null, "file:///C:WINDOWS\\CMD1.EXE"},
          new String[]{null, "file:///C|WINDOWS\\CMD2.EXE"},
          new String[]{null, "http://www.anomic.de/test/"},
          new String[]{null, "http://www.anomic.de/"},
          new String[]{null, "http://www.anomic.de"},
          new String[]{null, "http://www.anomic.de/home/test?x=1#home"},
          new String[]{null, "http://www.anomic.de/home/test?x=1"},
          new String[]{null, "http://www.anomic.de/home/test#home"},
          new String[]{null, "ftp://ftp.anomic.de/home/test#home"},
          new String[]{null, "http://www.anomic.de/home/../abc/"},
          new String[]{null, "mailto:abcdefg@nomailnomail.com"},
          new String[]{"http://www.anomic.de/home", "test"},
          new String[]{"http://www.anomic.de/home", "test/"},
          new String[]{"http://www.anomic.de/home/", "test"},
          new String[]{"http://www.anomic.de/home/", "test/"},
          new String[]{"http://www.anomic.de/home/index.html", "test.htm"},
          new String[]{"http://www.anomic.de/home/index.html", "http://www.yacy.net/test"},
          new String[]{"http://www.anomic.de/home/index.html", "ftp://ftp.yacy.net/test"},
          new String[]{"http://www.anomic.de/home/index.html", "../test"},
          new String[]{"http://www.anomic.de/home/index.html", "mailto:abcdefg@nomailnomail.com"},
          new String[]{null, "news:de.test"},
          new String[]{"http://www.anomic.de/home", "news:de.test"},
          new String[]{null, "mailto:bob@web.com"},
          new String[]{"http://www.anomic.de/home", "mailto:bob@web.com"},
          new String[]{"http://www.anomic.de/home", "ftp://ftp.anomic.de/src"},
          new String[]{null, "ftp://ftp.delegate.org/"},
          new String[]{"http://www.anomic.de/home", "ftp://ftp.delegate.org/"},
          new String[]{"http://www.anomic.de","mailto:yacy@weltherrschaft.org"},
          new String[]{"http://www.anomic.de","javascipt:temp"},
          new String[]{null,"http://yacy-websuche.de/wiki/index.php?title=De:IntroInformationFreedom&action=history"},
          new String[]{null, "http://diskusjion.no/index.php?s=5bad5f431a106d9a8355429b81bb0ca5&showuser=23585"},
          new String[]{null, "http://diskusjion.no/index.php?s=5bad5f431a106d9a8355429b81bb0ca5&amp;showuser=23585"},
          new String[]{null, "http://www.scc.kit.edu/publikationen/80.php?PHPSESSID=5f3624d3e1c33d4c086ab600d4d5f5a1"},
          new String[]{null, "smb://localhost/"},
          new String[]{null, "smb://localhost/repository"}, // paths must end with '/'
          new String[]{null, "smb://localhost/repository/"},
          new String[]{null, "\\\\localhost\\"}, // Windows-like notion of smb shares
          new String[]{null, "\\\\localhost\\repository"},
          new String[]{null, "\\\\localhost\\repository\\"}
          };
        URI.initSessionIDNames(new File("defaults/sessionid.names"));
        String environment, url;
        URI aURL, aURL1;
        java.net.URL jURL;
        for (int i = 0; i < test.length; i++) {
            environment = test[i][0];
            url = test[i][1];
            try {aURL = URI.newURL(environment, url);} catch (final MalformedURLException e) {Log.logException(e); aURL = null;}
            if (aURL != null) System.out.println("normalized: " + aURL.toNormalform(true, true,  true));
            if (environment == null) {
                try {jURL = new java.net.URL(url);} catch (final MalformedURLException e) {jURL = null;}
            } else {
                try {jURL = new java.net.URL(new java.net.URL(environment), url);} catch (final MalformedURLException e) {jURL = null;}
            }
            
            // check equality to java.net.URL
            if (((aURL == null) && (jURL != null)) ||
                ((aURL != null) && (jURL == null)) ||
                ((aURL != null) && (jURL != null) && (!(jURL.toString().equals(aURL.toString()))))) {
                System.out.println("Difference for environment=" + environment + ", url=" + url + ":");
                System.out.println((jURL == null) ? "jURL rejected input" : "jURL=" + jURL.toString());
                System.out.println((aURL == null) ? "aURL rejected input" : "aURL=" + aURL.toString());
            }
            
            // check stability: the normalform of the normalform must be equal to the normalform
            if (aURL != null) try {
                aURL1 = new URI(aURL.toNormalform(false, true));
                if (!(aURL1.toNormalform(false, true).equals(aURL.toNormalform(false, true)))) {
                    System.out.println("no stability for url:");
                    System.out.println("aURL0=" + aURL.toString());
                    System.out.println("aURL1=" + aURL1.toString());
                }
            } catch (final MalformedURLException e) {
                System.out.println("no stability for url:");
                System.out.println("aURL0=" + aURL.toString());
                System.out.println("aURL1 cannot be computed:" + e.getMessage());
            }
        }
    }
}
