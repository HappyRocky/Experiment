package utils.sensitiveWordFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * @className:SensitiveWordFilter.java
 * @classDescription: 敏感词过滤器
 * @author:GongYanshang
 * @createTime:2017年10月10日
 */
public class SensitiveWordFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        // 获取敏感词库
        Map sensitiveWordMap = SensitivewordUtil.getSensitiveWordMap(request);
        if (sensitiveWordMap != null && !sensitiveWordMap.isEmpty()) {
        	try {
    			Map map = new HashMap(request.getParameterMap());
    			
    			// 遍历所有请求参数
    			if (map != null) {
    				Iterator<Entry> it = map.entrySet().iterator();
    				while (it.hasNext()) {
    					Entry entry = it.next();
    					Object key = entry.getKey();
    					Object value = entry.getValue();
    					
    					// String类型的参数值，则进行过滤
    					if (value != null) {
    						if (value instanceof String) {
    							String afterValue = SensitivewordUtil.replaceSensitiveWord(value.toString(), 1, "*", sensitiveWordMap); // 最小匹配规则
    							map.put(key, afterValue);
    						} else if (value instanceof Object[]) {
    							Object[] values = (Object[]) value;
    							for (int i = 0; i < values.length; i++) {
    								Object curValue = values[i];
    								if (curValue instanceof String) {
    									String afterValue = SensitivewordUtil.replaceSensitiveWord(curValue.toString(), 1, "*", sensitiveWordMap); // 最小匹配规则
    									values[i] = afterValue;
    								}
    							}
    							map.put(key, values);
    						}
    					}
    				}
    				
    				// 包装request，否则不允许修改原生态request
    				request = new ParameterRequestWrapper(request, map);
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
		}
        chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		
	}

}

