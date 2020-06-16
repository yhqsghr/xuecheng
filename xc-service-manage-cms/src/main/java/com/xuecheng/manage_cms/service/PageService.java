package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author YHQ
 * @date 2019/12/23 10:04
 */
@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    //接口调用
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 分页查询
     *
     * @return
     */
    public Page<CmsPage> findList(int page,
                                  int size,
                                  QueryPageRequest queryPageRequest) {

        //条件值
        CmsPage cmsPage = new CmsPage();
        //站点ID
        String siteId = queryPageRequest.getSiteId();
        if (StringUtils.isNotEmpty(siteId)) {
            cmsPage.setSiteId(siteId);
        }
        //模板ID
        String templateId = queryPageRequest.getTemplateId();
        if (StringUtils.isNotEmpty(templateId)) {
            cmsPage.setTemplateId(templateId);
        }
        //页面类型
        String pageType = queryPageRequest.getPageType();
        if (StringUtils.isNotEmpty(pageType)) {
            cmsPage.setPageType(pageType);
        }
        //模糊查询页面别名
        String pageAliase = queryPageRequest.getPageAliase();
        if (StringUtils.isNotEmpty(pageAliase)) {
            cmsPage.setPageAliase(pageAliase);
        }
        //模糊查询页面名
        String pageName = queryPageRequest.getPageName();
        if (StringUtils.isNotEmpty(pageName)) {
            cmsPage.setPageName(pageName);
        }
        //条件匹配器
        ExampleMatcher Matcher = ExampleMatcher.matching()
                .withMatcher(
                        "pageAliase",
                        ExampleMatcher.GenericPropertyMatchers.contains()
                );
        //封装数据cmsPage到Example
        Example<CmsPage> example = Example.of(cmsPage, Matcher);
        //方便用户理解当前页,实际操作还是-1
        page--;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        return all;
    }

    /**
     * 增加  cmspage
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //在所有可预测异常前手动判断
        if (cmsPage == null) {
            return new CmsPageResult(CommonCode.FAIL, null);
        }
        String pageName = cmsPage.getPageName();
        String pageWebPath = cmsPage.getPageWebPath();
        String siteId = cmsPage.getSiteId();


        //添加前先查询是否该页面已经存在 校验条件 由3个字段组成的联合索引是否存在
        CmsPage _cmspage = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(siteId, pageName, pageWebPath);
        if (_cmspage != null) {
            //页面已存在，提示用户不要重复添加
            //1. 0 -> return new CmsPageResult(CommonCode.FAIL,null);
            //手动抛出用户自定义异常
            //2. 0 -> throw new CustomException(CommonCode.FAIL);
            //页面已存在直接修改原页面信息并提示已更新页面信息
            //this.edit(cmsPage.getPageId(),cmsPage);
            //手动抛出异常提示页面已存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //通过重重判断后,业务代码放在最后
        try {
            cmsPage = cmsPageRepository.save(cmsPage);
        } catch (CustomException e) {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_ADDFAILE);
        }

        //引入统一异常处理，从上面可以看出 我们抛出的异常最后会抛到方法调用处，如果方法调用处不处理最后异常会抛给jvm但是我们得到的提示信息仅仅就是spring框架给我们封装好的异常提示
        //用户也无法从中得到很好的提示，只是知道服务端出现了错误，所以我们引入统一异常处理类去捕获异常，向用户返回统一规范的响应信息
        //添加成功
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    /**
     * 保存页面  如果已存在则更新页面
     */
    public CmsPageResult add(CmsPage cmsPage) {
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPage1 = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            //更新
            return this.edit(cmsPage1.getPageId(), cmsPage);
        } else {
            //添加
            return this.save(cmsPage);
        }
    }


    /**
     * 根据id查询页面
     *
     * @param id
     * @return
     */
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            //存在
            return optional.get();
        } else {
            return null;
        }
    }

    /**
     * 修改页面
     *
     * @param cmsPage
     * @return siteId: '',
     * templateId: '',
     * pageName: '',
     * pageAliase: '',
     * pageWebPath: '',
     * pageParameter: '',
     * pagePhysicalPath: '',
     * pageType: '',
     * pageCreateTime: new Date()
     */
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        CmsPage cmsPageDemo = this.findById(id);
        cmsPageDemo.setPageParameter(cmsPage.getPageParameter());
        cmsPageDemo.setTemplateId(cmsPage.getTemplateId());
        cmsPageDemo.setPageName(cmsPage.getPageName());
        cmsPageDemo.setPageAliase(cmsPage.getPageAliase());
        cmsPageDemo.setPageType(cmsPage.getPageType());
        cmsPageDemo.setPageWebPath(cmsPage.getPageWebPath());
        cmsPageDemo.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
        cmsPageDemo.setPageCreateTime(cmsPage.getPageCreateTime());
        cmsPageDemo.setDataUrl(cmsPage.getDataUrl());

        try {
            CmsPage saveCmsPage = cmsPageRepository.save(cmsPageDemo);
            //添加成功
            return new CmsPageResult(CommonCode.SUCCESS, saveCmsPage);
        } catch (Exception e) {
            e.printStackTrace();
            //失败
            return new CmsPageResult(CommonCode.FAIL, null);
        }
    }

    /**
     * 删除页面
     *
     * @param id
     * @return
     */
    public ResponseResult delete(String id) {
        try {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
    }


    /**
     * ===========================================页面预览静态化===========================================================
     * ===========================================页面预览静态化===========================================================
     * ===========================================页面预览静态化===========================================================
     * 静态化页面方法
     * 静态化程序获取页面的DataUrl
     * <p>
     * 静态化程序远程请求DataUrl获取数据模型。
     * <p>
     * 静态化程序获取页面的模板信息
     * <p>
     * 执行页面静态化
     */
    public String getHtml(String pageId) {
        //获取模板和数据
        String template = this.getTemplate(pageId);
        Map<String, Object> modelByPageId = this.getModelByPageId(pageId);

        //模板引擎初始化模板执行静态化
        // 1.创建配置对象  
        Configuration configuration = new Configuration(Configuration.getVersion());
        //2.模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", template);
        //3.向配置对象中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            Template configurationTemplate = configuration.getTemplate("template");
            //执行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(configurationTemplate, modelByPageId);
            //返回页面html
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            //失败返回null
            return null;
        }
    }

    /**
     * 1.从gridfs获取模板
     *
     * @param pageId
     * @return
     */
    public String getTemplate(String pageId) {
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模板文件内容
            //根据文件id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource对象，获取流
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 2.获取模板数据
     *
     * @param pageId
     * @return
     */
    public Map<String, Object> getModelByPageId(String pageId) {
        CmsPage cmsPage = this.findById(pageId);
        String dataUrl = cmsPage.getDataUrl();
        ResponseEntity<Map> entity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = entity.getBody();
        return body;
    }


    /**
     * 根据id查询配置管理信息
     *
     * @param pageId
     * @return
     */
    public CmsConfig getConfigById(String pageId) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(pageId);
        if (optional.isPresent()) {
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    /**
     * ===========================================发布页面===============================================================
     * ===========================================发布页面===============================================================
     * ===========================================发布页面===============================================================
     *
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId) {
        //1.页面静态化
        String htmlContent = this.getHtml(pageId);
        //2.将静态化的内容保存到gridfs文件系统中
        InputStream htmlInputStream = null;
        try {
            //把html页面内容转换为流对象
            htmlInputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //根据pageid查询pagename
            CmsPage cmsPage = this.findById(pageId);
            String pageName = cmsPage.getPageName();
            //返回的是已经保存在gridfs中的文件id htmlfileId
            ObjectId objectId = gridFsTemplate.store(htmlInputStream, pageName);
            //更新cmspage中的文件id=fileHtmlid int -> 16进制字符串
            cmsPage.setHtmlFileId(objectId.toHexString());
            //保存新的cmspage页面
            cmsPageRepository.save(cmsPage);
            //3.发送mq消息 pageid 表示文件已经存储随时来取
            this.postMsg(pageId);
            return new ResponseResult(CommonCode.SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    /**
     * 根据pageid删除存放在gridfs中的发布页面的信息
     *
     * @param htmlFileId
     */
    public void deleteHtmlFile(String htmlFileId) {
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
    }


    /**
     * mq发送消息
     *
     * @param pageId
     */
    public void postMsg(String pageId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("pageId", pageId);
        //转成json串
        String jsonString = JSON.toJSONString(map);
        //发送消息 routingkey = siteId
        CmsPage cmsPage = this.findById(pageId);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, cmsPage.getSiteId(), jsonString);
    }


    /**
     * 一键发布页面接口
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //根据服务端传递过来的cmspage添加到服务器更路径 并返回url 页面物理路径 + pageName
        //保存cmspage 无则添加有则修改
        CmsPageResult cmsPageResult = this.add(cmsPage);
        CmsPage save_cmspage = cmsPageResult.getCmsPage();
        String pageId = save_cmspage.getPageId();
        //页面发布=静态化+替换文件
        this.postPage(pageId);
        String url = cmsPage.getPageWebPath() + cmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, url);
    }

    /**
     * 根据courseid +“.html” 获取cmspage对象
     *
     * @param pageName
     * @return
     */
    public CmsPage findByPageName(String pageName) {
        if (StringUtils.isEmpty(pageName)) {
            return null;
        }
        return cmsPageRepository.findByPageName(pageName);
    }

}

