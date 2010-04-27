/**
 *  Extension.java
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

import java.util.HashMap;
import java.util.Map;

public enum Extension {

    SEVENZIP("7z"),
    AI("ai"),
    AIFF("aiff"),
    ASP("asp"),
    ASPX("aspx"),
    AU("au"),
    AVI("avi"),
    BAT("bat"),
    BIN("bin"),
    BMP("bmp"),
    BZ2("bz2"),
    CLASS("class"),
    C("c"),
    COM("com"),
    CSS("css"),
    CSV("csv"),
    DB("db"),
    DLL("dll"),
    DOC("doc"),
    DOCX("docx"),
    DOT("dot"),
    DVI("dvi"),
    EPS("eps"),
    EXE("exe"),
    GIF("gif"),
    GZ("gz"),
    HQX("hqx"),
    HTM("htm"),
    HTML("html"),
    ICO("ico"),
    JAR("jar"),
    JAVA("java"),
    JPE("jpe"),
    JPEG("jpeg"),
    JPG("jpg"),
    JS("js"),
    JSP("jsp"),
    LHA("lha"),
    LZH("lzh"),
    M4V("m4v"),
    MOV("mov"),
    MP2("mp2"),
    MP3("mp3"),
    MP4("mp4"),
    MPE("mpe"),
    MPEG("mpeg"),
    MPG("mpg"),
    ODT("odt"),
    ODS("ods"),
    ODP("odp"),
    ODG("odg"),
    ODC("odc"),
    ODF("odf"),
    ODB("odb"),
    ODI("odi"),
    ODM("odm"),
    OPML("opml"),
    OTT("ott"),
    OTS("ots"),
    OTP("otp"),
    OTG("otg"),
    OGG("ogg"),
    PAC("pac"),
    PDF("pdf"),
    PHP("php"),
    PHTML("phtml"),
    PL("pl"),
    PNG("png"),
    POT("pot"),
    PPS("pps"),
    PPT("ppt"),
    PPTX("pptx"),
    PPZ("ppz"),
    PS("ps"),
    PY("py"),
    QT("qt"),
    RA("ra"),
    RAM("ram"),
    RAR("rar"),
    RDF("rdf"),
    RM("rm"),
    RPM("rpm"),
    RSS("rss"),
    RTF("rtf"),
    SH("sh"),
    SHTML("shtml"),
    STREAM("stream"),
    SVG("svg"),
    SWF("swf"),
    SXW("sxw"),
    SXC("sxc"),
    SXD("sxd"),
    SXI("sxi"),
    TAR("tar"),
    TBZ("tbz"),
    TBZ2("tbz2"),
    TEX("tex"),
    TGZ("tgz"),
    TIF("tif"),
    TIFF("tiff"),
    TORRENT("torrent"),
    TXT("txt"),
    VCARD("vcard"),
    VDX("vdx"),
    VTX("vtx"),
    VCF("vcf"),
    VSD("vsd"),
    VST("vst"),
    VSS("vss"),
    WAV("wav"),
    WMV("wmv"),
    XHTML("xhtml"),
    XLA("xla"),
    XLS("xls"),
    XLSX("xlsx"),
    XPI("xpi"),
    XSL("xsl"),
    XML("xml"),
    Z("Z"),
    ZIP("zip");
    
    private String ext;
    private static Map<Extension, MimeType> mimeMapper = new HashMap<Extension, MimeType>();
    private static Map<String, Extension> extMapper = new HashMap<String, Extension>();
    
    static {
        mimeMapper.put(SXC, MimeType.APPLICATION_OO_CALC);
        mimeMapper.put(SXD, MimeType.APPLICATION_OO_DRAW);
        mimeMapper.put(SXI, MimeType.APPLICATION_OO_IMPRESS);
        mimeMapper.put(SXW, MimeType.APPLICATION_OO_WRITER);
        mimeMapper.put(GZ, MimeType.APPLICATION_GZIP);
        mimeMapper.put(JAR, MimeType.APPLICATION_JAVA_ARCHIVE);
        mimeMapper.put(BIN, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(PDF, MimeType.APPLICATION_PDF);
        mimeMapper.put(AI, MimeType.APPLICATION_POSTSCRIPT);
        mimeMapper.put(EPS, MimeType.APPLICATION_POSTSCRIPT);
        mimeMapper.put(PS, MimeType.APPLICATION_POSTSCRIPT);
        mimeMapper.put(RDF, MimeType.APPLICATION_RDF_XML);
        mimeMapper.put(RSS, MimeType.APPLICATION_RSS_XML);
        mimeMapper.put(RTF, MimeType.APPLICATION_RTF);
        mimeMapper.put(TAR, MimeType.APPLICATION_TAR);
        mimeMapper.put(TGZ, MimeType.APPLICATION_TAR);
        mimeMapper.put(SEVENZIP, MimeType.APPLICATION_SEVENZIP_COMPRESSED);
        mimeMapper.put(TORRENT, MimeType.APPLICATION_BITTORRENT);
        mimeMapper.put(BZ2, MimeType.APPLICATION_BZIP2);
        mimeMapper.put(TBZ, MimeType.APPLICATION_BZIP2);
        mimeMapper.put(TBZ2, MimeType.APPLICATION_BZIP2);
        mimeMapper.put(Z, MimeType.APPLICATION_COMPRESS);
        mimeMapper.put(DVI, MimeType.APPLICATION_DVI);
        mimeMapper.put(PHP, MimeType.APPLICATION_HTTPD_PHP);
        mimeMapper.put(PHTML, MimeType.APPLICATION_HTTPD_PHP);
        mimeMapper.put(LZH, MimeType.APPLICATION_LZH);
        mimeMapper.put(LHA, MimeType.APPLICATION_LZH);
        mimeMapper.put(PAC, MimeType.APPLICATION_NS_PROXY_AUTOCONFIG);
        mimeMapper.put(RAR, MimeType.APPLICATION_RAR);
        mimeMapper.put(RPM, MimeType.APPLICATION_RPM);
        mimeMapper.put(SWF, MimeType.APPLICATION_SHOCKWAVE_FLASH);
        mimeMapper.put(TEX, MimeType.APPLICATION_TEX);
        mimeMapper.put(VDX, MimeType.APPLICATION_VISIO);
        mimeMapper.put(VTX, MimeType.APPLICATION_VISIO);
        mimeMapper.put(VSD, MimeType.APPLICATION_VISIO);
        mimeMapper.put(VST, MimeType.APPLICATION_VISIO);
        mimeMapper.put(VSS, MimeType.APPLICATION_VISIO);
        mimeMapper.put(XPI, MimeType.APPLICATION_XPINSTALL);
        mimeMapper.put(ZIP, MimeType.APPLICATION_ZIP);
        mimeMapper.put(HQX, MimeType.APPLICATION_MAXC_BINHEX40);
        mimeMapper.put(XLA, MimeType.APPLICATION_MSEXCEL);
        mimeMapper.put(XLS, MimeType.APPLICATION_MSEXCEL);
        mimeMapper.put(XLSX, MimeType.APPLICATION_MSEXCEL);
        mimeMapper.put(POT, MimeType.APPLICATION_MSPOWERPOINT);
        mimeMapper.put(PPT, MimeType.APPLICATION_MSPOWERPOINT);
        mimeMapper.put(PPS, MimeType.APPLICATION_MSPOWERPOINT);
        mimeMapper.put(PPTX, MimeType.APPLICATION_MSPOWERPOINT);
        mimeMapper.put(PPZ, MimeType.APPLICATION_MSPOWERPOINT);
        mimeMapper.put(DB, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(DLL, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(EXE, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(CLASS, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(COM, MimeType.APPLICATION_OCTET_STREAM);
        mimeMapper.put(ODC, MimeType.APPLICATION_OPENDOCUMENT_CHART);
        mimeMapper.put(ODB, MimeType.APPLICATION_OPENDOCUMENT_DATABASE);
        mimeMapper.put(ODF, MimeType.APPLICATION_OPENDOCUMENT_FORMULA);
        mimeMapper.put(ODG, MimeType.APPLICATION_OPENDOCUMENT_GRAPHICS);
        mimeMapper.put(OTG, MimeType.APPLICATION_OPENDOCUMENT_GRAPHICS_TEMPLATE);
        mimeMapper.put(ODI, MimeType.APPLICATION_OPENDOCUMENT_IMAGE);
        mimeMapper.put(ODP, MimeType.APPLICATION_OPENDOCUMENT_PRESENTATION);
        mimeMapper.put(OTP, MimeType.APPLICATION_OPENDOCUMENT_PRESENTATION_TEMPLATE);
        mimeMapper.put(ODS, MimeType.APPLICATION_OPENDOCUMENT_SPREADSHEET);
        mimeMapper.put(OTS, MimeType.APPLICATION_OPENDOCUMENT_SPREADSHEET_TEMPLATE);
        mimeMapper.put(ODT, MimeType.APPLICATION_OPENDOCUMENT_TEXT);
        mimeMapper.put(ODM, MimeType.APPLICATION_OPENDOCUMENT_TEXT_MASTER);
        mimeMapper.put(OTT, MimeType.APPLICATION_OPENDOCUMENT_TEXT_TEMPLATE);
        mimeMapper.put(AU, MimeType.AUDIO_BASIC);
        mimeMapper.put(MP2, MimeType.AUDIO_MPEG);
        mimeMapper.put(MP3, MimeType.AUDIO_MPEG);
        mimeMapper.put(OGG, MimeType.AUDIO_OGG_VORBIS);
        mimeMapper.put(AIFF, MimeType.AUDIO_AIFF);
        mimeMapper.put(RA, MimeType.AUDIO_REALAUDIO);
        mimeMapper.put(RAM, MimeType.AUDIO_REALAUDIO);
        mimeMapper.put(RM, MimeType.AUDIO_REALAUDIO_PLUGIN);
        mimeMapper.put(STREAM, MimeType.AUDIO_QT_STREAM);
        mimeMapper.put(WAV, MimeType.AUDIO_WAV);
        mimeMapper.put(BMP, MimeType.IMAGE_BMP);
        mimeMapper.put(GIF, MimeType.IMAGE_GIF);
        mimeMapper.put(JPE, MimeType.IMAGE_JPEG);
        mimeMapper.put(JPG, MimeType.IMAGE_JPEG);
        mimeMapper.put(JPEG, MimeType.IMAGE_JPEG);
        mimeMapper.put(PNG, MimeType.IMAGE_PNG);
        mimeMapper.put(SVG, MimeType.IMAGE_SVG_XML);
        mimeMapper.put(TIF, MimeType.IMAGE_TIFF);
        mimeMapper.put(TIFF, MimeType.IMAGE_TIFF);
        mimeMapper.put(CSS, MimeType.TEXT_CSS);
        mimeMapper.put(CSV, MimeType.TEXT_CSV);
        mimeMapper.put(ASP, MimeType.TEXT_HTML);
        mimeMapper.put(ASPX, MimeType.TEXT_HTML);
        mimeMapper.put(HTM, MimeType.TEXT_HTML);
        mimeMapper.put(HTML, MimeType.TEXT_HTML);
        mimeMapper.put(JSP, MimeType.TEXT_HTML);
        mimeMapper.put(SHTML, MimeType.TEXT_HTML);
        mimeMapper.put(PY, MimeType.TEXT_HTML);
        mimeMapper.put(JS, MimeType.TEXT_JAVASCRIPT);
        mimeMapper.put(C, MimeType.TEXT_PLAIN);
        mimeMapper.put(BAT, MimeType.TEXT_PLAIN);
        mimeMapper.put(TXT, MimeType.TEXT_PLAIN);
        mimeMapper.put(JAVA, MimeType.TEXT_PLAIN);
        mimeMapper.put(PL, MimeType.TEXT_PLAIN);
        mimeMapper.put(SH, MimeType.TEXT_PLAIN);
        mimeMapper.put(OPML, MimeType.TEXT_OPML);
        mimeMapper.put(RTF, MimeType.TEXT_RTF);
        mimeMapper.put(VCARD, MimeType.TEXT_VCARD);
        mimeMapper.put(XHTML, MimeType.TEXT_XHTML_XML);
        mimeMapper.put(XML, MimeType.TEXT_XML);
        mimeMapper.put(MP4, MimeType.VIDEO_MP4);
        mimeMapper.put(MPE, MimeType.VIDEO_MPEG);
        mimeMapper.put(MPEG, MimeType.VIDEO_MPEG);
        mimeMapper.put(MPG, MimeType.VIDEO_MPEG);
        mimeMapper.put(MOV, MimeType.VIDEO_QUICKGTIME);
        mimeMapper.put(QT, MimeType.VIDEO_QUICKGTIME);
        mimeMapper.put(M4V, MimeType.VIDEO_M4V);
        mimeMapper.put(WMV, MimeType.VIDEO_WMV);
        mimeMapper.put(AVI, MimeType.VIDEO_MSVIDEO);
        
        for (Extension ext: Extension.values()) {
            extMapper.put(ext.name().toLowerCase(), ext);
        }
    }
    
    
    private Extension(final String ext) {
        this.ext = ext;
    }

    public String getFileNameExtension() {
        return ext;
    }
    
    public static MimeType getMimeType(Extension ext) {
        return mimeMapper.get(ext);
    }
    
    public static Extension getFileNameExtension(String extString) {
        return extMapper.get(extString.toLowerCase());
    }
    
}
