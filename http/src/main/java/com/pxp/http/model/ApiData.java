package com.pxp.http.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pxp.http.cb.ApiBasicListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 请求参数封装类
 */
public class ApiData {

    private ApiData() {
    }

    //接口标志
    private ApiTag tag;
    //请求回调
    private ApiBasicListener basicListener;
    //返回数据是否是数组结构
    private boolean isList = false;
    //请求参数
    private Map<String, Object> params;
    //请求body
    private String body;
    //请求头
    private Map<String, String> header;
    //是否无视设定好的解析字段
    private boolean isNoKey;
    //该值不为空的情况下会替换基础host
    private String replaceHost;

    public String getBody() {
        return body;
    }

    public ApiTag getTag() {
        return tag;
    }

    public ApiBasicListener getBasicListener() {
        return basicListener;
    }

    public boolean isList() {
        return isList;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public boolean isNoKey() {
        return isNoKey;
    }

    public String getReplaceHost() {
        return replaceHost;
    }

    public static class ApiDataBuilder {
        private ApiTag tag;
        private ApiBasicListener basicListener;
        private boolean isList = false;
        private Map<String, Object> params;
        private String body;
        private Map<String, String> header;
        private boolean isNoKey;
        private String replaceHost;

        public ApiDataBuilder setTag(ApiTag tag) {
            this.tag = tag;
            return this;
        }

        public ApiDataBuilder setApiBasicListener(ApiBasicListener basicListener) {
            this.basicListener = basicListener;
            return this;
        }

        public ApiDataBuilder isList(boolean isList) {
            this.isList = isList;
            return this;
        }

        public ApiDataBuilder setParams(Object bean) {
            Gson gson = new Gson();
            String json = gson.toJson(bean);
            this.params = fromJson(json, new TypeToken<Map<String, Object>>() {
            });
            return this;
        }

        public ApiDataBuilder setParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public ApiDataBuilder setBody(Object bean) {
            Gson gson = new Gson();
            this.body = gson.toJson(bean);
            return this;
        }

        public ApiDataBuilder setBody(Map<String, Object> body) {
            this.body = new Gson().toJson(body);
            return this;
        }

        public ApiDataBuilder setHeader(Map<String, String> header) {
            this.header = header;
            return this;
        }

        public ApiDataBuilder isNoKey(boolean isNoKey) {
            this.isNoKey = isNoKey;
            return this;
        }

        public ApiDataBuilder setReplaceHost(String replaceHost) {
            this.replaceHost = replaceHost;
            return this;
        }

        public ApiData builder() {
            ApiData apiData = new ApiData();
            apiData.tag = tag;
            apiData.basicListener = basicListener;
            apiData.isList = isList;
            apiData.params = params;
            apiData.body = body;
            apiData.header = header;
            apiData.isNoKey = isNoKey;
            apiData.replaceHost = replaceHost;
            return apiData;
        }
    }

    private static <T> Map<String, Object> fromJson(String json, TypeToken<T> typeToken) {
        Gson gson = new GsonBuilder().registerTypeAdapter(
                new TypeToken<Map<String, Object>>() {
                }.getType(), new TypeAdapter<Object>() {
                    @Override
                    public void write(JsonWriter out, Object value) throws IOException {

                    }

                    @Override
                    public Object read(JsonReader in) throws IOException {
                        JsonToken token = in.peek();
                        switch (token) {
                            case BEGIN_ARRAY:
                                List<Object> list = new ArrayList<>();
                                in.beginArray();
                                while (in.hasNext()) {
                                    list.add(read(in));
                                }
                                in.endArray();
                                return list;
                            case BEGIN_OBJECT:
                                Map<String, Object> map = new LinkedTreeMap<>();
                                in.beginObject();
                                while (in.hasNext()) {
                                    map.put(in.nextName(), read(in));
                                }
                                in.endObject();
                                return map;
                            case STRING:
                                return in.nextString();
                            case NUMBER:
                                //改写数字处理逻辑,避免int变化为浮点
                                double dbNUM = in.nextDouble();
                                if (dbNUM > Long.MAX_VALUE)
                                    return dbNUM;
                                long longNum = (long) dbNUM;
                                if (dbNUM == longNum)
                                    return longNum;
                                else
                                    return dbNUM;
                            case BOOLEAN:
                                return in.nextBoolean();
                            case NULL:
                                in.nextNull();
                                return null;
                            default:
                                throw new IllegalStateException();
                        }
                    }
                }).create();
        return gson.fromJson(json, typeToken.getType());
    }
}
