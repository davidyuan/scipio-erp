/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.base.util;

import org.owasp.esapi.codecs.CSSCodec;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.HTMLEntityCodec;
import org.owasp.esapi.codecs.PercentCodec;
import org.owasp.esapi.codecs.XMLEntityCodec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilCodec {
    private static final String module = UtilCodec.class.getName();
    private static final HtmlEncoder htmlEncoder = new HtmlEncoder();
    private static final XmlEncoder xmlEncoder = new XmlEncoder();
    private static final StringEncoder stringEncoder = new StringEncoder();
    /**
     * SCIPIO: Css encoder (new).
     */
    private static final CssEncoder cssEncoder = new CssEncoder();
    /**
     * SCIPIO: Raw/none encoder that returns the original string as-is. Useful as workaround.
     */
    private static final RawEncoder rawEncoder = new RawEncoder();
    
    private static final UrlCodec urlCodec = new UrlCodec();
    private static final List<Codec> codecs;
    static {
        List<Codec> tmpCodecs = new ArrayList<Codec>();
        tmpCodecs.add(new HTMLEntityCodec());
        tmpCodecs.add(new PercentCodec());
        codecs = Collections.unmodifiableList(tmpCodecs);
    }
    
    /**
     * SCIPIO: list of available encoder names.
     */
    private static final Set<String> encoderNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
            "raw", "url", "xml", "html", "css", "string"
    })));
    
    /**
     * SCIPIO: list of available decoder names.
     */
    private static final Set<String> decoderNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
            "url"
    })));

    public static class IntrusionException extends GeneralRuntimeException {
        public IntrusionException(String message) {
            super(message);
        }
    }

    public static interface SimpleEncoder {
        public String encode(String original);
    }

    public static interface SimpleDecoder {
        public String decode(String original);
    }

    public static class HtmlEncoder implements SimpleEncoder {
        private static final char[] IMMUNE_HTML = {',', '.', '-', '_', ' '};
        private HTMLEntityCodec htmlCodec = new HTMLEntityCodec();
        public String encode(String original) {
            if (original == null) {
                return null;
            }
            return htmlCodec.encode(IMMUNE_HTML, original);
        }
    }

    public static class XmlEncoder implements SimpleEncoder {
        private static final char[] IMMUNE_XML = {',', '.', '-', '_', ' '};
        private XMLEntityCodec xmlCodec = new XMLEntityCodec();
        public String encode(String original) {
            if (original == null) {
                return null;
            }
            return xmlCodec.encode(IMMUNE_XML, original);
        }
    }

    public static class UrlCodec implements SimpleEncoder, SimpleDecoder {
        public String encode(String original) {
            try {
                return URLEncoder.encode(original, "UTF-8");
            } catch (UnsupportedEncodingException ee) {
                Debug.logError(ee, module);
                return null;
            }
        }

        public String decode(String original) {
            try {
                String canonical = canonicalize(original);
                return URLDecoder.decode(canonical, "UTF-8");
            } catch (UnsupportedEncodingException ee) {
                Debug.logError(ee, module);
                return null;
            }
        }
    }

    public static class StringEncoder implements SimpleEncoder {
        public String encode(String original) {
            if (original != null) {
                original = original.replace("\"", "\\\"");
            }
            return original;
        }
    }

    /**
     * SCIPIO: CSS part encoder.
     */
    public static class CssEncoder implements SimpleEncoder {
        private static final char[] IMMUNE_CSS = {',', '.', '-', '_', ' '}; // safe?: , '%'
        private CSSCodec cssCodec = new CSSCodec();
        public String encode(String original) {
            if (original == null) {
                return null;
            }
            return cssCodec.encode(IMMUNE_CSS, original);
        }
    }
    
    /**
     * SCIPIO: Raw/none encoder that returns the original string as-is. Useful as workaround.
     */
    public static class RawEncoder implements SimpleEncoder {
        public String encode(String original) {
            return original;
        }
    }
    
    
    // ================== Begin General Functions ==================

    public static SimpleEncoder getEncoder(String type) {
        // SCIPIO: Raw/none encoder that returns the original string as-is. Useful as workaround and to simplify code.
        if ("raw".equals(type)) {
            return rawEncoder;
        } else if ("url".equals(type)) {
            return urlCodec;
        } else if ("xml".equals(type)) {
            return xmlEncoder;
        } else if ("html".equals(type)) {
            return htmlEncoder;
        } else if ("css".equals(type)) { // SCIPIO: new
            return cssEncoder;
        } else if ("string".equals(type)) {
            return stringEncoder;
        } else {
            return null;
        }
    }
    
    /**
     * SCIPIO: Returns raw/dummy encoder (quick method).
     * <p>
     * NOTE: Generic SimpleEncoder type is returned as opposed to RawEncoder in order to 
     * preserve the abstraction provided by {@link #getEncoder(String)}.
     */
    public static SimpleEncoder getRawEncoder() {
        return rawEncoder;
    }
    
    /**
     * SCIPIO: Returns html encoder (quick method).
     */
    public static SimpleEncoder getHtmlEncoder() {
        return htmlEncoder;
    }
    
    /**
     * SCIPIO: Returns xml encoder (quick method).
     */
    public static SimpleEncoder getXmlEncoder() {
        return xmlEncoder;
    }
    
    /**
     * SCIPIO: Returns url encoder (quick method).
     */
    public static SimpleEncoder getUrlEncoder() {
        return urlCodec;
    }
    
    /**
     * SCIPIO: Returns css encoder (quick method).
     */
    public static SimpleEncoder getCssEncoder() {
        return cssEncoder;
    }
    
    /**
     * SCIPIO: Checks if encoder is raw encoder (abstraction).
     */
    public static boolean isRawEncoder(SimpleEncoder encoder) {
        return (encoder instanceof RawEncoder);
    }
    
    /**
     * SCIPIO: Checks if encoder is null or raw encoder (abstraction).
     */
    public static boolean isRawOrNullEncoder(SimpleEncoder encoder) {
        return (encoder == null) || (encoder instanceof RawEncoder);
    }

    public static SimpleDecoder getDecoder(String type) {
        if ("url".equals(type)) {
            return urlCodec;
        } else {
            return null;
        }
    }
    
    /**
     * SCIPIO: Returns url decoder (quick method).
     */
    public static SimpleDecoder getUrlDecoder() {
        return urlCodec;
    }
    
    /**
     * SCIPIO: Returns all available encoder names.
     */
    public static Set<String> getEncoderNames() {
        return encoderNames;
    }
    
    /**
     * SCIPIO: Returns all available decoder names.
     */
    public static Set<String> getDecoderNames() {
        return decoderNames;
    }
    
    /**
     * SCIPIO: Quick encoding method.
     */
    public static String encode(String lang, String value) {
        return getEncoder(lang).encode(value);
    }
    
    /**
     * SCIPIO: Quick decoding method.
     */
    public static String decode(String lang, String value) {
        return getDecoder(lang).decode(value);
    }

    public static String canonicalize(String value) throws IntrusionException {
        return canonicalize(value, false, false);
    }

    public static String canonicalize(String value, boolean strict) throws IntrusionException {
        return canonicalize(value, strict, strict);
    }

    public static String canonicalize(String input, boolean restrictMultiple, boolean restrictMixed) {
        if (input == null) {
            return null;
        }

        String working = input;
        Codec codecFound = null;
        int mixedCount = 1;
        int foundCount = 0;
        boolean clean = false;
        while (!clean) {
            clean = true;

            // try each codec and keep track of which ones work
            Iterator i = codecs.iterator();
            while (i.hasNext()) {
                Codec codec = (Codec) i.next();
                String old = working;
                working = codec.decode(working);
                if (!old.equals(working)) {
                    if (codecFound != null && codecFound != codec) {
                        mixedCount++;
                    }
                    codecFound = codec;
                    if (clean) {
                        foundCount++;
                    }
                    clean = false;
                }
            }
        }

        // do strict tests and handle if any mixed, multiple, nested encoding were found
        if (foundCount >= 2 && mixedCount > 1) {
            if (restrictMultiple || restrictMixed) {
                throw new IntrusionException("Input validation failure");
            } else {
                Debug.logWarning("Multiple (" + foundCount + "x) and mixed encoding (" + mixedCount + "x) detected in " + input, module);
            }
        } else if (foundCount >= 2) {
            if (restrictMultiple) {
                throw new IntrusionException("Input validation failure");
            } else {
                Debug.logWarning("Multiple (" + foundCount + "x) encoding detected in " + input, module);
            }
        } else if (mixedCount > 1) {
            if (restrictMixed) {
                throw new IntrusionException("Input validation failure");
            } else {
                Debug.logWarning("Mixed encoding (" + mixedCount + "x) detected in " + input, module);
            }
        }
        return working;
    }

    /**
     * Uses a black-list approach for necessary characters for HTML.
     * Does not allow various characters (after canonicalization), including "<", ">", "&" (if not followed by a space), and "%" (if not followed by a space).
     *
     * @param value
     * @param errorMessageList
     */
    public static String checkStringForHtmlStrictNone(String valueName, String value, List<String> errorMessageList) {
        if (UtilValidate.isEmpty(value)) return value;

        // canonicalize, strict (error on double-encoding)
        try {
            value = canonicalize(value, true);
        } catch (IntrusionException e) {
            // NOTE: using different log and user targeted error messages to allow the end-user message to be less technical
            Debug.logError("Canonicalization (format consistency, character escaping that is mixed or double, etc) error for attribute named [" + valueName + "], String [" + value + "]: " + e.toString(), module);
            errorMessageList.add("In field [" + valueName + "] found character escaping (mixed or double) that is not allowed or other format consistency error: " + e.toString());
        }

        // check for "<", ">"
        if (value.indexOf("<") >= 0 || value.indexOf(">") >= 0) {
            errorMessageList.add("In field [" + valueName + "] less-than (<) and greater-than (>) symbols are not allowed.");
        }

        /* NOTE DEJ 20090311: After playing with this more this doesn't seem to be necessary; the canonicalize will convert all such characters into actual text before this check is done, including other illegal chars like &lt; which will canonicalize to < and then get caught
        // check for & followed a semicolon within 7 characters, no spaces in-between (and perhaps other things sometime?)
        int curAmpIndex = value.indexOf("&");
        while (curAmpIndex > -1) {
            int semicolonIndex = value.indexOf(";", curAmpIndex + 1);
            int spaceIndex = value.indexOf(" ", curAmpIndex + 1);
            if (semicolonIndex > -1 && (semicolonIndex - curAmpIndex <= 7) && (spaceIndex < 0 || (spaceIndex > curAmpIndex && spaceIndex < semicolonIndex))) {
                errorMessageList.add("In field [" + valueName + "] the ampersand (&) symbol is only allowed if not used as an encoded character: no semicolon (;) within 7 spaces or there is a space between.");
                // once we find one like this we have the message so no need to check for more
                break;
            }
            curAmpIndex = value.indexOf("&", curAmpIndex + 1);
        }
         */

        /* NOTE DEJ 20090311: After playing with this more this doesn't seem to be necessary; the canonicalize will convert all such characters into actual text before this check is done, including other illegal chars like %3C which will canonicalize to < and then get caught
        // check for % followed by 2 hex characters
        int curPercIndex = value.indexOf("%");
        while (curPercIndex >= 0) {
            if (value.length() > (curPercIndex + 3) && UtilValidate.isHexDigit(value.charAt(curPercIndex + 1)) && UtilValidate.isHexDigit(value.charAt(curPercIndex + 2))) {
                errorMessageList.add("In field [" + valueName + "] the percent (%) symbol is only allowed if followed by a space.");
                // once we find one like this we have the message so no need to check for more
                break;
            }
            curPercIndex = value.indexOf("%", curPercIndex + 1);
        }
         */

        // TODO: anything else to check for that can be used to get HTML or JavaScript going without these characters?

        return value;
    }

    /**
     * A simple Map wrapper class that will do HTML encoding. To be used for passing a Map to something that will expand Strings with it as a context, etc.
     * <p>
     * SCIPIO: changed from HtmlEncodingMapWrapper to EncodingMapWrapper to remove bias.
     */
    public static class EncodingMapWrapper<K> implements Map<K, Object> {
        public static <K> EncodingMapWrapper<K> getEncodingMapWrapper(Map<K, Object> mapToWrap, SimpleEncoder encoder) {
            if (mapToWrap == null) return null;

            EncodingMapWrapper<K> mapWrapper = new EncodingMapWrapper<K>();
            mapWrapper.setup(mapToWrap, encoder);
            return mapWrapper;
        }

        protected Map<K, Object> internalMap = null;
        protected SimpleEncoder encoder = null;
        protected EncodingMapWrapper() { }

        public void setup(Map<K, Object> mapToWrap, SimpleEncoder encoder) {
            this.internalMap = mapToWrap;
            this.encoder = encoder;
        }
        public void reset() {
            this.internalMap = null;
            this.encoder = null;
        }

        public int size() { return this.internalMap.size(); }
        public boolean isEmpty() { return this.internalMap.isEmpty(); }
        public boolean containsKey(Object key) { return this.internalMap.containsKey(key); }
        public boolean containsValue(Object value) { return this.internalMap.containsValue(value); }
        public Object get(Object key) {
            Object theObject = this.internalMap.get(key);
            if (theObject instanceof String) {
                if (this.encoder != null) {
                    return encoder.encode((String) theObject);
                } else {
                    // SCIPIO: removed HTML bias
                    //return UtilCodec.getEncoder("html").encode((String) theObject);
                    return (String) theObject;
                }
            } else if (theObject instanceof Map<?, ?>) {
                return EncodingMapWrapper.getEncodingMapWrapper(UtilGenerics.<K, Object>checkMap(theObject), this.encoder);
            }
            return theObject;
        }
        public Object put(K key, Object value) { return this.internalMap.put(key, value); }
        public Object remove(Object key) { return this.internalMap.remove(key); }
        public void putAll(Map<? extends K, ? extends Object> arg0) { this.internalMap.putAll(arg0); }
        public void clear() { this.internalMap.clear(); }
        public Set<K> keySet() { return this.internalMap.keySet(); }
        public Collection<Object> values() { return this.internalMap.values(); }
        public Set<Map.Entry<K, Object>> entrySet() { return this.internalMap.entrySet(); }
        @Override
        public String toString() { return this.internalMap.toString(); }
    }
    
    /**
     * SCIPIO: the original HtmlEncodingMapWrapper, as a specialization.
     */
    public static class HtmlEncodingMapWrapper<K> extends EncodingMapWrapper<K> {
        public static <K> HtmlEncodingMapWrapper<K> getHtmlEncodingMapWrapper(Map<K, Object> mapToWrap, SimpleEncoder encoder) {
            if (mapToWrap == null) return null;

            HtmlEncodingMapWrapper<K> mapWrapper = new HtmlEncodingMapWrapper<K>();
            mapWrapper.setup(mapToWrap, encoder);
            return mapWrapper;
        }

        public Object get(Object key) {
            Object theObject = this.internalMap.get(key);
            if (theObject instanceof String) {
                if (this.encoder != null) {
                    return encoder.encode((String) theObject);
                } else {
                    // SCIPIO: go direct
                    //return UtilCodec.getEncoder("html").encode((String) theObject);
                    return UtilCodec.getHtmlEncoder().encode((String) theObject);
                }
            } else if (theObject instanceof Map<?, ?>) {
                return HtmlEncodingMapWrapper.getHtmlEncodingMapWrapper(UtilGenerics.<K, Object>checkMap(theObject), this.encoder);
            }
            return theObject;
        }
    }

}
