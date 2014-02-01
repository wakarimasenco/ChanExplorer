package co.wakarimasen.chanexplorer;

import java.util.Hashtable;

/**
 * Collection of static methods to convert special and extended
 * characters into HTML entitities and vice versa.<br/><br/>
 * Copyright (c) 2004-2005 Tecnick.com S.r.l (www.tecnick.com) Via Ugo Foscolo
 * n.19 - 09045 Quartu Sant'Elena (CA) - ITALY - www.tecnick.com -
 * info@tecnick.com<br/>
 * Project homepage: <a href="http://htmlentities.sourceforge.net" target="_blank">http://htmlentities.sourceforge.net</a><br/>
 * License: http://www.gnu.org/copyleft/lesser.html LGPL
 * @author Nicola Asuni [www.tecnick.com].
 * @version 1.0.004
 */
public class HTMLEntities {
	
	/**
	 * Translation table for HTML entities.<br/>
	 * reference: W3C - Character entity references in HTML 4 [<a href="http://www.w3.org/TR/html401/sgml/entities.html" target="_blank">http://www.w3.org/TR/html401/sgml/entities.html</a>].
	 */
	private static final Object[][] html_entities_table = {
		{ new String("&Aacute;"), Integer.valueOf(193) },
		{ new String("&aacute;"), Integer.valueOf(225) },
		{ new String("&Acirc;"), Integer.valueOf(194) },
		{ new String("&acirc;"), Integer.valueOf(226) },
		{ new String("&acute;"), Integer.valueOf(180) },
		{ new String("&AElig;"), Integer.valueOf(198) },
		{ new String("&aelig;"), Integer.valueOf(230) },
		{ new String("&Agrave;"), Integer.valueOf(192) },
		{ new String("&agrave;"), Integer.valueOf(224) },
		{ new String("&alefsym;"), Integer.valueOf(8501) },
		{ new String("&Alpha;"), Integer.valueOf(913) },
		{ new String("&alpha;"), Integer.valueOf(945) },
		{ new String("&amp;"), Integer.valueOf(38) },
		{ new String("&and;"), Integer.valueOf(8743) },
		{ new String("&ang;"), Integer.valueOf(8736) },
		{ new String("&Aring;"), Integer.valueOf(197) },
		{ new String("&aring;"), Integer.valueOf(229) },
		{ new String("&asymp;"), Integer.valueOf(8776) },
		{ new String("&Atilde;"), Integer.valueOf(195) },
		{ new String("&atilde;"), Integer.valueOf(227) },
		{ new String("&Auml;"), Integer.valueOf(196) },
		{ new String("&auml;"), Integer.valueOf(228) },
		{ new String("&bdquo;"), Integer.valueOf(8222) },
		{ new String("&Beta;"), Integer.valueOf(914) },
		{ new String("&beta;"), Integer.valueOf(946) },
		{ new String("&brvbar;"), Integer.valueOf(166) },
		{ new String("&bull;"), Integer.valueOf(8226) },
		{ new String("&cap;"), Integer.valueOf(8745) },
		{ new String("&Ccedil;"), Integer.valueOf(199) },
		{ new String("&ccedil;"), Integer.valueOf(231) },
		{ new String("&cedil;"), Integer.valueOf(184) },
		{ new String("&cent;"), Integer.valueOf(162) },
		{ new String("&Chi;"), Integer.valueOf(935) },
		{ new String("&chi;"), Integer.valueOf(967) },
		{ new String("&circ;"), Integer.valueOf(710) },
		{ new String("&clubs;"), Integer.valueOf(9827) },
		{ new String("&cong;"), Integer.valueOf(8773) },
		{ new String("&copy;"), Integer.valueOf(169) },
		{ new String("&crarr;"), Integer.valueOf(8629) },
		{ new String("&cup;"), Integer.valueOf(8746) },
		{ new String("&curren;"), Integer.valueOf(164) },
		{ new String("&dagger;"), Integer.valueOf(8224) },
		{ new String("&Dagger;"), Integer.valueOf(8225) },
		{ new String("&darr;"), Integer.valueOf(8595) },
		{ new String("&dArr;"), Integer.valueOf(8659) },
		{ new String("&deg;"), Integer.valueOf(176) },
		{ new String("&Delta;"), Integer.valueOf(916) },
		{ new String("&delta;"), Integer.valueOf(948) },
		{ new String("&diams;"), Integer.valueOf(9830) },
		{ new String("&divide;"), Integer.valueOf(247) },
		{ new String("&Eacute;"), Integer.valueOf(201) },
		{ new String("&eacute;"), Integer.valueOf(233) },
		{ new String("&Ecirc;"), Integer.valueOf(202) },
		{ new String("&ecirc;"), Integer.valueOf(234) },
		{ new String("&Egrave;"), Integer.valueOf(200) },
		{ new String("&egrave;"), Integer.valueOf(232) },
		{ new String("&empty;"), Integer.valueOf(8709) },
		{ new String("&emsp;"), Integer.valueOf(8195) },
		{ new String("&ensp;"), Integer.valueOf(8194) },
		{ new String("&Epsilon;"), Integer.valueOf(917) },
		{ new String("&epsilon;"), Integer.valueOf(949) },
		{ new String("&equiv;"), Integer.valueOf(8801) },
		{ new String("&Eta;"), Integer.valueOf(919) },
		{ new String("&eta;"), Integer.valueOf(951) },
		{ new String("&ETH;"), Integer.valueOf(208) },
		{ new String("&eth;"), Integer.valueOf(240) },
		{ new String("&Euml;"), Integer.valueOf(203) },
		{ new String("&euml;"), Integer.valueOf(235) },
		{ new String("&euro;"), Integer.valueOf(8364) },
		{ new String("&exist;"), Integer.valueOf(8707) },
		{ new String("&fnof;"), Integer.valueOf(402) },
		{ new String("&forall;"), Integer.valueOf(8704) },
		{ new String("&frac12;"), Integer.valueOf(189) },
		{ new String("&frac14;"), Integer.valueOf(188) },
		{ new String("&frac34;"), Integer.valueOf(190) },
		{ new String("&frasl;"), Integer.valueOf(8260) },
		{ new String("&Gamma;"), Integer.valueOf(915) },
		{ new String("&gamma;"), Integer.valueOf(947) },
		{ new String("&ge;"), Integer.valueOf(8805) },
		{ new String("&harr;"), Integer.valueOf(8596) },
		{ new String("&hArr;"), Integer.valueOf(8660) },
		{ new String("&hearts;"), Integer.valueOf(9829) },
		{ new String("&hellip;"), Integer.valueOf(8230) },
		{ new String("&Iacute;"), Integer.valueOf(205) },
		{ new String("&iacute;"), Integer.valueOf(237) },
		{ new String("&Icirc;"), Integer.valueOf(206) },
		{ new String("&icirc;"), Integer.valueOf(238) },
		{ new String("&iexcl;"), Integer.valueOf(161) },
		{ new String("&Igrave;"), Integer.valueOf(204) },
		{ new String("&igrave;"), Integer.valueOf(236) },
		{ new String("&image;"), Integer.valueOf(8465) },
		{ new String("&infin;"), Integer.valueOf(8734) },
		{ new String("&int;"), Integer.valueOf(8747) },
		{ new String("&Iota;"), Integer.valueOf(921) },
		{ new String("&iota;"), Integer.valueOf(953) },
		{ new String("&iquest;"), Integer.valueOf(191) },
		{ new String("&isin;"), Integer.valueOf(8712) },
		{ new String("&Iuml;"), Integer.valueOf(207) },
		{ new String("&iuml;"), Integer.valueOf(239) },
		{ new String("&Kappa;"), Integer.valueOf(922) },
		{ new String("&kappa;"), Integer.valueOf(954) },
		{ new String("&Lambda;"), Integer.valueOf(923) },
		{ new String("&lambda;"), Integer.valueOf(955) },
		{ new String("&lang;"), Integer.valueOf(9001) },
		{ new String("&laquo;"), Integer.valueOf(171) },
		{ new String("&larr;"), Integer.valueOf(8592) },
		{ new String("&lArr;"), Integer.valueOf(8656) },
		{ new String("&lceil;"), Integer.valueOf(8968) },
		{ new String("&ldquo;"), Integer.valueOf(8220) },
		{ new String("&le;"), Integer.valueOf(8804) },
		{ new String("&lfloor;"), Integer.valueOf(8970) },
		{ new String("&lowast;"), Integer.valueOf(8727) },
		{ new String("&loz;"), Integer.valueOf(9674) },
		{ new String("&lrm;"), Integer.valueOf(8206) },
		{ new String("&lsaquo;"), Integer.valueOf(8249) },
		{ new String("&lsquo;"), Integer.valueOf(8216) },
		{ new String("&macr;"), Integer.valueOf(175) },
		{ new String("&mdash;"), Integer.valueOf(8212) },
		{ new String("&micro;"), Integer.valueOf(181) },
		{ new String("&middot;"), Integer.valueOf(183) },
		{ new String("&minus;"), Integer.valueOf(8722) },
		{ new String("&Mu;"), Integer.valueOf(924) },
		{ new String("&mu;"), Integer.valueOf(956) },
		{ new String("&nabla;"), Integer.valueOf(8711) },
		{ new String("&nbsp;"), Integer.valueOf(160) },
		{ new String("&ndash;"), Integer.valueOf(8211) },
		{ new String("&ne;"), Integer.valueOf(8800) },
		{ new String("&ni;"), Integer.valueOf(8715) },
		{ new String("&not;"), Integer.valueOf(172) },
		{ new String("&notin;"), Integer.valueOf(8713) },
		{ new String("&nsub;"), Integer.valueOf(8836) },
		{ new String("&Ntilde;"), Integer.valueOf(209) },
		{ new String("&ntilde;"), Integer.valueOf(241) },
		{ new String("&Nu;"), Integer.valueOf(925) },
		{ new String("&nu;"), Integer.valueOf(957) },
		{ new String("&Oacute;"), Integer.valueOf(211) },
		{ new String("&oacute;"), Integer.valueOf(243) },
		{ new String("&Ocirc;"), Integer.valueOf(212) },
		{ new String("&ocirc;"), Integer.valueOf(244) },
		{ new String("&OElig;"), Integer.valueOf(338) },
		{ new String("&oelig;"), Integer.valueOf(339) },
		{ new String("&Ograve;"), Integer.valueOf(210) },
		{ new String("&ograve;"), Integer.valueOf(242) },
		{ new String("&oline;"), Integer.valueOf(8254) },
		{ new String("&Omega;"), Integer.valueOf(937) },
		{ new String("&omega;"), Integer.valueOf(969) },
		{ new String("&Omicron;"), Integer.valueOf(927) },
		{ new String("&omicron;"), Integer.valueOf(959) },
		{ new String("&oplus;"), Integer.valueOf(8853) },
		{ new String("&or;"), Integer.valueOf(8744) },
		{ new String("&ordf;"), Integer.valueOf(170) },
		{ new String("&ordm;"), Integer.valueOf(186) },
		{ new String("&Oslash;"), Integer.valueOf(216) },
		{ new String("&oslash;"), Integer.valueOf(248) },
		{ new String("&Otilde;"), Integer.valueOf(213) },
		{ new String("&otilde;"), Integer.valueOf(245) },
		{ new String("&otimes;"), Integer.valueOf(8855) },
		{ new String("&Ouml;"), Integer.valueOf(214) },
		{ new String("&ouml;"), Integer.valueOf(246) },
		{ new String("&para;"), Integer.valueOf(182) },
		{ new String("&part;"), Integer.valueOf(8706) },
		{ new String("&permil;"), Integer.valueOf(8240) },
		{ new String("&perp;"), Integer.valueOf(8869) },
		{ new String("&Phi;"), Integer.valueOf(934) },
		{ new String("&phi;"), Integer.valueOf(966) },
		{ new String("&Pi;"), Integer.valueOf(928) },
		{ new String("&pi;"), Integer.valueOf(960) },
		{ new String("&piv;"), Integer.valueOf(982) },
		{ new String("&plusmn;"), Integer.valueOf(177) },
		{ new String("&pound;"), Integer.valueOf(163) },
		{ new String("&prime;"), Integer.valueOf(8242) },
		{ new String("&Prime;"), Integer.valueOf(8243) },
		{ new String("&prod;"), Integer.valueOf(8719) },
		{ new String("&prop;"), Integer.valueOf(8733) },
		{ new String("&Psi;"), Integer.valueOf(936) },
		{ new String("&psi;"), Integer.valueOf(968) },
		{ new String("&quot;"), Integer.valueOf(34) },
		{ new String("&radic;"), Integer.valueOf(8730) },
		{ new String("&rang;"), Integer.valueOf(9002) },
		{ new String("&raquo;"), Integer.valueOf(187) },
		{ new String("&rarr;"), Integer.valueOf(8594) },
		{ new String("&rArr;"), Integer.valueOf(8658) },
		{ new String("&rceil;"), Integer.valueOf(8969) },
		{ new String("&rdquo;"), Integer.valueOf(8221) },
		{ new String("&real;"), Integer.valueOf(8476) },
		{ new String("&reg;"), Integer.valueOf(174) },
		{ new String("&rfloor;"), Integer.valueOf(8971) },
		{ new String("&Rho;"), Integer.valueOf(929) },
		{ new String("&rho;"), Integer.valueOf(961) },
		{ new String("&rlm;"), Integer.valueOf(8207) },
		{ new String("&rsaquo;"), Integer.valueOf(8250) },
		{ new String("&rsquo;"), Integer.valueOf(8217) },
		{ new String("&sbquo;"), Integer.valueOf(8218) },
		{ new String("&Scaron;"), Integer.valueOf(352) },
		{ new String("&scaron;"), Integer.valueOf(353) },
		{ new String("&sdot;"), Integer.valueOf(8901) },
		{ new String("&sect;"), Integer.valueOf(167) },
		{ new String("&shy;"), Integer.valueOf(173) },
		{ new String("&Sigma;"), Integer.valueOf(931) },
		{ new String("&sigma;"), Integer.valueOf(963) },
		{ new String("&sigmaf;"), Integer.valueOf(962) },
		{ new String("&sim;"), Integer.valueOf(8764) },
		{ new String("&spades;"), Integer.valueOf(9824) },
		{ new String("&sub;"), Integer.valueOf(8834) },
		{ new String("&sube;"), Integer.valueOf(8838) },
		{ new String("&sum;"), Integer.valueOf(8721) },
		{ new String("&sup1;"), Integer.valueOf(185) },
		{ new String("&sup2;"), Integer.valueOf(178) },
		{ new String("&sup3;"), Integer.valueOf(179) },
		{ new String("&sup;"), Integer.valueOf(8835) },
		{ new String("&supe;"), Integer.valueOf(8839) },
		{ new String("&szlig;"), Integer.valueOf(223) },
		{ new String("&Tau;"), Integer.valueOf(932) },
		{ new String("&tau;"), Integer.valueOf(964) },
		{ new String("&there4;"), Integer.valueOf(8756) },
		{ new String("&Theta;"), Integer.valueOf(920) },
		{ new String("&theta;"), Integer.valueOf(952) },
		{ new String("&thetasym;"), Integer.valueOf(977) },
		{ new String("&thinsp;"), Integer.valueOf(8201) },
		{ new String("&THORN;"), Integer.valueOf(222) },
		{ new String("&thorn;"), Integer.valueOf(254) },
		{ new String("&tilde;"), Integer.valueOf(732) },
		{ new String("&times;"), Integer.valueOf(215) },
		{ new String("&trade;"), Integer.valueOf(8482) },
		{ new String("&Uacute;"), Integer.valueOf(218) },
		{ new String("&uacute;"), Integer.valueOf(250) },
		{ new String("&uarr;"), Integer.valueOf(8593) },
		{ new String("&uArr;"), Integer.valueOf(8657) },
		{ new String("&Ucirc;"), Integer.valueOf(219) },
		{ new String("&ucirc;"), Integer.valueOf(251) },
		{ new String("&Ugrave;"), Integer.valueOf(217) },
		{ new String("&ugrave;"), Integer.valueOf(249) },
		{ new String("&uml;"), Integer.valueOf(168) },
		{ new String("&upsih;"), Integer.valueOf(978) },
		{ new String("&Upsilon;"), Integer.valueOf(933) },
		{ new String("&upsilon;"), Integer.valueOf(965) },
		{ new String("&Uuml;"), Integer.valueOf(220) },
		{ new String("&uuml;"), Integer.valueOf(252) },
		{ new String("&weierp;"), Integer.valueOf(8472) },
		{ new String("&Xi;"), Integer.valueOf(926) },
		{ new String("&xi;"), Integer.valueOf(958) },
		{ new String("&Yacute;"), Integer.valueOf(221) },
		{ new String("&yacute;"), Integer.valueOf(253) },
		{ new String("&yen;"), Integer.valueOf(165) },
		{ new String("&yuml;"), Integer.valueOf(255) },
		{ new String("&Yuml;"), Integer.valueOf(376) },
		{ new String("&Zeta;"), Integer.valueOf(918) },
		{ new String("&zeta;"), Integer.valueOf(950) },
		{ new String("&zwj;"), Integer.valueOf(8205) },
		{ new String("&zwnj;"), Integer.valueOf(8204) } };
	
	/**
	 * Map to convert extended characters in html entities.
	 */
	private static final Hashtable<Object, Object> htmlentities_map = new Hashtable<Object, Object>();
	
	/**
	 * Map to convert html entities in exteden characters.
	 */
	private static final Hashtable<Object, Object> unhtmlentities_map = new Hashtable<Object, Object>();
	
	//==============================================================================
	// METHODS
	//==============================================================================
	
	/**
	 * Initialize HTML translation maps.
	 */
	public HTMLEntities() {
		initializeEntitiesTables();
	}
	
	/**
	 * Initialize HTML entities table.
	 */
	private static void initializeEntitiesTables() {
		// initialize html translation maps
		for (int i = 0; i < html_entities_table.length; ++i) {
			htmlentities_map.put(html_entities_table[i][1],
					html_entities_table[i][0]);
			unhtmlentities_map.put(html_entities_table[i][0],
					html_entities_table[i][1]);
		}
	}
	
	/**
	 * Get the html entities translation table.
	 * 
	 * @return translation table
	 */
	public static Object[][] getEntitiesTable() {
		return html_entities_table;
	}
	
	/**
	 * Convert special and extended characters into HTML entitities.
	 * @param str input string
	 * @return formatted string
	 * @see #unhtmlentities(String)
	 */
	public static String htmlentities(String str) {
		
		if (str == null) {
			return "";
		}
		//initialize html translation maps table the first time is called
		if (htmlentities_map.isEmpty()) {
			initializeEntitiesTables();
		}
		
		StringBuffer buf = new StringBuffer(); //the otput string buffer
		
		for (int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			String entity = (String) htmlentities_map.get(((int) ch)); //get equivalent html entity
			if (entity == null) { //if entity has not been found
				if ((ch) > 128) { //check if is an extended character
					buf.append("&#" + ((int) ch) + ";"); //convert extended character
				} else {
					buf.append(ch); //append the character as is
				}
			} else {
				buf.append(entity); //append the html entity
			}
		}
		return buf.toString();
	}
	
	/**
	 * Convert HTML entities to special and extended unicode characters
	 * equivalents.
	 * @param str input string
	 * @return formatted string
	 * @see #htmlentities(String)
	 */
	public static String unhtmlentities(String str) {
		
		//initialize html translation maps table the first time is called
		if (htmlentities_map.isEmpty()) {
			initializeEntitiesTables();
		}
		
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			if (ch == '&') {
				int semi = str.indexOf(';', i + 1);
				if ((semi == -1) || ((semi-i) > 7)){
					buf.append(ch);
					continue;
				}
				String entity = str.substring(i, semi + 1);
				Integer iso;
				if (entity.charAt(1) == ' ') {
					buf.append(ch);
					continue;
				}
				if (entity.charAt(1) == '#') {
					if (entity.charAt(2) == 'x') {
						iso = Integer.parseInt(entity.substring(3, entity.length() - 1), 16);
					}
					else {
						iso = Integer.parseInt(entity.substring(2, entity.length() - 1));
					}
				} else {
					iso = (Integer) unhtmlentities_map.get(entity);
				}
				if (iso == null) {
					buf.append(entity);
				} else {
					buf.append((char) (iso.intValue()));
				}
				i = semi;
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}
	
	// methods to convert special characters
	
	/**
	 * Replace single quotes characters with HTML entities.
	 * 
	 * @param str the input string
	 * @return string with replaced single quotes
	 */
	public static String htmlSingleQuotes(String str) {
		str = str.replaceAll("[\']", "&rsquo;");
		str = str.replaceAll("&#039;", "&rsquo;");
		str = str.replaceAll("&#145;", "&rsquo;");
		str = str.replaceAll("&#146;", "&rsquo;");
		return str;
	}
	
	/**
	 * Replace single quotes HTML entities with equivalent character.
	 * 
	 * @param str the input string
	 * @return string with replaced single quotes
	 */
	public static String unhtmlSingleQuotes(String str) {
		return str.replaceAll("&rsquo;", "\'");
	}
	
	/**
	 * Replace double quotes characters with HTML entities.
	 * 
	 * @param str the input string
	 * @return string with replaced double quotes
	 */
	public static String htmlDoubleQuotes(String str) {
		str = str.replaceAll("[\"]", "&quot;");
		str = str.replaceAll("&#147;", "&quot;");
		str = str.replaceAll("&#148;", "&quot;");
		return str;
	}
	
	/**
	 * Replace single quotes HTML entities with equivalent character.
	 * 
	 * @param str the input string
	 * @return string with replaced single quotes
	 */
	public static String unhtmlDoubleQuotes(String str) {
		return str.replaceAll("&quot;", "\"");
	}
	
	/**
	 * Replace single and double quotes characters with HTML entities.
	 * 
	 * @param str the input string
	 * @return string with replaced quotes
	 */
	public static String htmlQuotes(String str) {
		str = htmlDoubleQuotes(str); //convert double quotes
		str = htmlSingleQuotes(str); //convert single quotes
		return str;
	}
	
	/**
	 * Replace single and double quotes HTML entities with equivalent characters.
	 * 
	 * @param str the input string
	 * @return string with replaced quotes
	 */
	public static String unhtmlQuotes(String str) {
		str = unhtmlDoubleQuotes(str); //convert double quotes
		str = unhtmlSingleQuotes(str); //convert single quotes
		return str;
	}
	
	/**
	 * Replace &lt; &gt; characters with &amp;lt; &amp;gt; entities.
	 * 
	 * @param str the input string
	 * @return string with replaced characters
	 */
	public static String htmlAngleBrackets(String str) {
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		return str;
	}
	
	/**
	 * Replace &amp;lt; &amp;gt; entities with &lt; &gt; characters.
	 * 
	 * @param str the input string
	 * @return string with replaced entities
	 */
	public static String unhtmlAngleBrackets(String str) {
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		return str;
	}
	
	/**
	 * Replace &amp; characters with &amp;amp; HTML entities.
	 * 
	 * @param str the input string
	 * @return string with replaced characters
	 */
	public static String htmlAmpersand(String str) {
		return str.replaceAll("&", "&amp;");
	}
	
	/**
	 * Replace &amp;amp; HTML entities with &amp; characters.
	 * 
	 * @param str the input string
	 * @return string with replaced entities
	 */
	public static String unhtmlAmpersand(String str) {
		return str.replaceAll("&amp;", "&");
	}
}
