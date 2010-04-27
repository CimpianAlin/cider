/**
 *  MimeType.java
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

public enum MimeType {

    APPLICATION_BITTORRENT("application/x-bittorrent"),
    APPLICATION_BZIP2("application/x-bzip2"),
    APPLICATION_COMPRESS("application/x-compress"),
    APPLICATION_DVI("application/x-dvi"),
    APPLICATION_GZIP("application/gzip"),
    APPLICATION_HTTPD_PHP("application/x-httpd-php"),
    APPLICATION_JAVA_ARCHIVE("application/java-archive"),
    APPLICATION_LZH("application/x-lzh"),
    APPLICATION_MAXC_BINHEX40("application/mac-binhex40"),
    APPLICATION_MSEXCEL("application/msexcel"),
    APPLICATION_MSEXCEL_2("application/vnd.ms-excel"), 
    APPLICATION_MSPOWERPOINT("application/mspowerpoint"),
    APPLICATION_MSPOWERPOINT_2 ("application/vnd.ms-powerpoint"),
    APPLICATION_MSWORD("application/msword"),
    APPLICATION_NS_PROXY_AUTOCONFIG("application/x-ns-proxy-autoconfig"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_OO_CALC("application/OOo-calc"),
    APPLICATION_OO_DRAW("application/OOo-draw"),
    APPLICATION_OO_IMPRESS("application/OOo-impress"),
    APPLICATION_OO_WRITER("application/OOo-writer"),
    APPLICATION_OPENDOCUMENT_CHART("application/vnd.oasis.opendocument.chart"),
    APPLICATION_OPENDOCUMENT_DATABASE("application/vnd.oasis.opendocument.database"),
    APPLICATION_OPENDOCUMENT_FORMULA("application/vnd.oasis.opendocument.formula"),
    APPLICATION_OPENDOCUMENT_GRAPHICS("application/vnd.oasis.opendocument.graphics"),
    APPLICATION_OPENDOCUMENT_GRAPHICS_TEMPLATE("application/vnd.oasis.opendocument.graphics-template"),
    APPLICATION_OPENDOCUMENT_IMAGE("application/vnd.oasis.opendocument.image"),
    APPLICATION_OPENDOCUMENT_PRESENTATION("application/vnd.oasis.opendocument.presentation"),
    APPLICATION_OPENDOCUMENT_PRESENTATION_TEMPLATE("application/vnd.oasis.opendocument.presentation-template"),
    APPLICATION_OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet"),
    APPLICATION_OPENDOCUMENT_SPREADSHEET_TEMPLATE("application/vnd.oasis.opendocument.spreadsheet-template"),
    APPLICATION_OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text"),
    APPLICATION_OPENDOCUMENT_TEXT_MASTER("application/vnd.oasis.opendocument.text-master"),
    APPLICATION_OPENDOCUMENT_TEXT_TEMPLATE("application/vnd.oasis.opendocument.text-template"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_XPDF("application/x-pdf"),
    APPLICATION_ACROBAT("application/acrobat"),
    APPLICATION_VNDPDF("applications/vnd.pdf"),
    APPLICATION_POSTSCRIPT("application/postscript"),
    APPLICATION_RAR("application/x-rar"),
    APPLICATION_RDF_XML("application/rdf+xml"),
    APPLICATION_RPM("application/x-rpm"),
    APPLICATION_RSS_XML("application/rss+xml"),
    APPLICATION_RTF("application/rtf"),
    APPLICATION_SEVENZIP_COMPRESSED("application/x-7z-compressed"),
    APPLICATION_SHOCKWAVE_FLASH("application/x-shockwave-flash"),
    APPLICATION_TAR("application/tar"),
    APPLICATION_TEX("application/x-tex"),
    APPLICATION_VISIO("application/x-visio"),
    APPLICATION_VISIO_2("application/vnd.visio"),
    APPLICATION_XPINSTALL("application/x-xpinstall"),
    APPLICATION_ZIP("application/zip"),
    AUDIO_AIFF("audio/x-aiff"),
    AUDIO_BASIC("audio/basic"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_OGG_VORBIS("audio/ogg-vorbis"),
    AUDIO_QT_STREAM("audio/x-qt-stream"),
    AUDIO_REALAUDIO("audio/x-pn-realaudio"),
    AUDIO_REALAUDIO_PLUGIN("audio/x-pn-realaudio-plugin"),
    AUDIO_WAV("audio/x-wav"),
    IMAGE_BMP("image/bmp"),
    IMAGE_GIF("image/gif"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_SVG_XML("image/svg+xml"),
    IMAGE_TIFF("image/tiff"),
    TEXT_CSS("text/css"),
    TEXT_CSV("text/csv"),
    TEXT_HTML("text/html"),
    TEXT_JAVASCRIPT("text/javascript"),
    TEXT_OPML("text/x-opml"),
    TEXT_PLAIN("text/plain"),
    TEXT_PDF("text/pdf"),
    TEXT_XPDF("text/x-pdf"),
    TEXT_RTF("text/rtf"),
    TEXT_VCARD("text/x-vcard"),
    TEXT_XHTML_XML("text/xhtml+xml"),
    TEXT_XML("text/xml"),
    VIDEO_M4V("video/x-m4v"),
    VIDEO_MP4("video/mp4"),
    VIDEO_MPEG("video/mpeg"),
    VIDEO_MSVIDEO("video/x-msvideo"),
    VIDEO_QUICKGTIME("video/quicktime"),
    VIDEO_WMV("video/x-ms-wmv");
    
    private String mimetype;
    
    private MimeType(final String mimetype) {
        this.mimetype = mimetype;
    }

    public String getMimetype() {
        return mimetype;
    }   
    
    /**
     * Find the MIME-type for the given content type
     * @param contentType
     * @return the MIME-type or null
     */
    static public MimeType getMimetype(String contentType) {
        if (contentType == null) return MimeType.APPLICATION_OCTET_STREAM;
        contentType = contentType.toLowerCase();
        final int pos = contentType.indexOf(';');
        contentType = ((pos < 0) ? contentType.trim() : contentType.substring(0, pos).trim());
        for (MimeType mimetype : MimeType.values()) {
            if (contentType.startsWith(mimetype.getMimetype())) {
                return mimetype;
            }
        }
        
        return null;
    }

}
