package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.client.FileSystemClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YHQ
 * @date 2020/1/5 21:04
 */
@SuppressWarnings("ALL")
@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    //注入fegin客户端
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    FileSystemClient fileSystemClient;

    @Autowired
    FileSystemRepository fileSystemRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;


    /**
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }

    /**
     * 添加教学计划
     *
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //此时有俩种情况 teachplan中的parentid 为null 或者 不为null；当为null时默认为根节点 ；什么是根节点 就是父节点为0的node
        //一个课程对应一个课程计划
        //校验课程id和课程计划名称
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出父id
        String parentid = teachplan.getParentid();
        //如果上级id为空则为根节点
        if (StringUtils.isEmpty(parentid)) {
            //如果父结点为空则获取根结点,并储存
            this.saveTeachPlanIfRootIdIsNull(courseid, teachplan);
        } else {
            //父节点不为null则直接储存teachplan
            //设置grade等级
            teachplan.setGrade("3");
            teachplanRepository.save(teachplan);
        }
        //修改状态
        this.updateCourseStatus(courseid);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 取出根节点，并储存
     *
     * @param courseid
     * @return
     */
    @Transactional
    public void saveTeachPlanIfRootIdIsNull(String courseid, Teachplan input_teachplan) {
        //校验课程id 此处是为了防止多用户操作时 这边添加课程计划 ，别处删除了该课程就会出现npe异常
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (!optional.isPresent()) {
            //直接返回参数异常
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //判断根节点是否存在 判断标准：是否 courseid+ parentId=“0” 有对应的teachPlan对应 这里的最顶级节点代表课程信息 grade =1 parentid= 0
        Teachplan teachplan = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (teachplan != null) {
            //如果不为null说明根节点存在，即根节点id作为此teachplan的parentid
            String parentId = teachplan.getId();
            //设置用户输入的teachplan根节点id
            input_teachplan.setParentid(parentId);
            input_teachplan.setGrade("2");
            //添加更新过parentid的input_teachplan
            teachplanRepository.save(input_teachplan);
        }
    }

    /**
     * 分页查询课程列表
     * 需要返回的值：
     * courses: [
     * {
     * id:'4028e581617f945f01617f9dabc40000',//课程id
     * name:'test01',//课程名
     * pic:''//课程图片
     * },
     * {
     * id:'test02',
     * name:'test02',
     * pic:''
     * }
     * ]
     *
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }

        //设置分页参数
        PageHelper.startPage(page, size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> list = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setTotal(total);
        courseInfoQueryResult.setList(list);
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS, courseInfoQueryResult);

    }

    /**
     * 保存课程图片到course_pic库中
     *
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        //校验参数
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        if (StringUtils.isEmpty(pic)) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIS_URLISNULL);
        }

        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //coureseid为主键所以不允许重复、save即为update
        coursePicRepository.save(coursePic);
        //修改状态
        this.updateCourseStatus(courseId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 回显图片信息
     *
     * @param courseId
     * @return
     */
    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> byId = coursePicRepository.findById(courseId);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;
    }

    /**
     * 删除课程图片
     *
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {

        try {
            //删除mysql中的图片数据
            Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
            if (coursePicOptional.isPresent()) {
                coursePicRepository.deleteById(courseId);
                //修改状态
                this.updateCourseStatus(courseId);
                CoursePic coursePic = coursePicOptional.get();
                String pic = coursePic.getPic();
                //删除服务器中保存的图片
                fileSystemClient.delete(pic);
                ////删除mongodb_filesystem中保存的图片记录
                fileSystemRepository.deleteById(pic);
                return new ResponseResult(CommonCode.SUCCESS);
            }
            return new ResponseResult(CommonCode.FAIL);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    /**
     * 查询课程分类
     *
     * @return
     */
    public CategoryNode findCourseCatrgory() {
        CategoryNode categoryNode = courseCategoryMapper.findAll();
        return categoryNode;
    }

    /**
     * 课程预览
     * CourseBase courseBase;//基础信息
     * CourseMarket courseMarket;//课程营销
     * CoursePic coursePic;//课程图片
     * com.xuecheng.framework.domain.course.ext.TeachplanNode TeachplanNode;//教学计划
     *
     * @param id
     * @return
     */
    public CourseView courseview(String id) {
        CourseView courseView = new CourseView();
        Optional<CourseBase> courseBase = courseBaseRepository.findById(id);
        if (courseBase.isPresent()) {
            courseView.setCourseBase(courseBase.get());
        }
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(id);
        if (courseMarket.isPresent()) {
            courseView.setCourseMarket(courseMarket.get());
        }
        Optional<CoursePic> coursePic = coursePicRepository.findById(id);
        if (coursePic.isPresent()) {
            courseView.setCoursePic(coursePic.get());
        }
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        if (teachplanNode != null) {
            courseView.setTeachplanNode(teachplanNode);
        }
        return courseView;
    }

    /**
     * 课程预览
     *
     * @param id
     * @return
     */
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;
    @Value("${course-publish.pageType}")
    private String pageType;

    /**
     * 课程预览
     *
     * @param id
     * @return
     */
    @Transactional
    public CoursePublishResult preview(String id) {
        //用户点击预览按钮,获取页面预览链接
        //分两种情况 首先我们要知道 cmspage要和每一门课程一一对应 ,其中cmspage.pageName=courseId+.html;cmspageAliase=courseName
        //1.mysql中的course在mongodb中已有对应的cmspage对应,通过cmspage对象中的dataurl可以获取对应的courseview对象,我们点击预览按钮会生成cms微服务返回的url预览链接
        //url预览链接 = http://www.xuecheng.com/cms/preview/ + 5a92141cb00ffc5a448ff1a0(cmspageId)
        //如果已经存在该cmspage页面,调用cmspageClient.sava方法就可以返回cmspage对象
        CmsPage cmsPage = cmsPageClient.findByPageName(id + ".html");
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        //取出id
        CmsPage add_cmspage = cmsPageResult.getCmsPage();
        String url = previewUrl + add_cmspage.getPageId();
        //拼接url返回
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    /**
     * 课程发布
     *
     * @param id
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String id) {
        //获取课程
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        //修改课程状态 202001 (制作中) -> 202002(已发布)
        CourseBase courseBase = optionalCourseBase.get();
        courseBase.setStatus("202002");
        //保存课程
        courseBaseRepository.save(courseBase);
        CoursePub coursePub = this.createCoursePub(id);
        //设置时间戳 Date
        coursePub.setTimestamp(new Date());
        //设置发布时间 String
        coursePub.setPubTime(new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date()));
        coursePubRepository.save(coursePub);

        //根据课程id 传递cmspage给cms一键发布接口
        CmsPage cmsPage = cmsPageClient.findByPageName(id + ".html");
        //远程调用方法
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        String pageUrl = cmsPostPageResult.getPageUrl();
        //向teachplanpub表中保存课程媒资信息
        this.saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }


    /**
     * //向teachplanMediaPub中保存课程媒资信息
     *
     * @param courseId
     */
    private void saveTeachplanMediaPub(String courseId) {
        //先删除teachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //从teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将teachplanMediaList数据放到teachplanMediaPubs中
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        //将teachplanMediaList插入到teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    /**
     * 创建课程pub对象并赋值
     *
     * @param id
     */
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        //拷贝课程基础信息
        Optional<CourseBase> op = courseBaseRepository.findById(id);
        if (op.isPresent()) {
            CourseBase courseBase = op.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //拷贝图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()) {
            BeanUtils.copyProperties(picOptional.get(), coursePub);
        }
        //拷贝营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            BeanUtils.copyProperties(marketOptional.get(), coursePub);
        }
        //拷贝教学计划
        TeachplanNode teachplanList = this.findTeachplanList(id);
        //bean -> string
        String teachPlan = JSON.toJSONString(teachplanList);
        coursePub.setTeachplan(teachPlan);
        return coursePub;
    }

    /**
     * 抽取方法 根据courseid获取cmspage的方法
     */
    public CmsPage getCmspage(String courseId) {
        //根据id查询课程
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        CourseBase courseBase = optionalCourseBase.get();
        String courseName = courseBase.getName();
        String pageName = courseId + ".html";
        CmsPage new_cmsPage = new CmsPage();
        new_cmsPage.setDataUrl(publish_dataUrlPre + courseId);
        new_cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        new_cmsPage.setPageWebPath(publish_page_webpath);
        new_cmsPage.setSiteId(publish_siteId);
        new_cmsPage.setTemplateId(publish_templateId);
        new_cmsPage.setPageType(pageType);
        new_cmsPage.setPageCreateTime(new Date());
        new_cmsPage.setPageName(pageName);
        new_cmsPage.setPageAliase("课程详情页面_" + courseName);
        return new_cmsPage;
    }

    /**
     * 添加课程信息
     * {
     * "id": "",
     * "name": "1",
     * "users": "1",
     * "grade": "200001",
     * "studymodel": "201001",
     * "mt": "前端开发",
     * "st": "HTML/CSS",
     * "description": "1"
     * }
     *
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //根据mt和st的值去category表中查询对应id
        Map<String, String> map = courseCategoryMapper.getMtAndStByName(courseBase.getMt(), courseBase.getSt());
        if (map != null) {
            String mtid = map.get("mtid");
            String stid = map.get("stid");
            courseBase.setMt(mtid);
            courseBase.setSt(stid);
            //新课程 状态设为待发布
            courseBase.setStatus("202001");
            //保存
            CourseBase save = courseBaseRepository.save(courseBase);
            //添加课程计划信息
            Teachplan teachplan = new Teachplan();
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setParentid("0");
            teachplan.setStatus("0");
            teachplan.setId(courseBase.getId());
            teachplanRepository.save(teachplan);
            //添加课程至mongodb中 且cms微服务中正好有添加cmspage的接口
            CmsPage cmspage = this.getCmspage(save.getId());
            CmsPageResult addResult = cmsPageClient.add(cmspage);
            if (addResult.isSuccess()) {
                //如果mongodb中cmspage对象也添加成功了则返回success
                return new AddCourseResult(CommonCode.SUCCESS, save.getId());
            }
            return new AddCourseResult(CommonCode.FAIL, null);
        }
        return new AddCourseResult(CommonCode.FAIL, null);
    }

    /**
     * 查询回显课程页面
     *
     * @param courseId
     * @return
     */
    public CourseBase getCoursebaseById(String courseId) {
        Optional<CourseBase> op = courseBaseRepository.findById(courseId);
        if (!op.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CourseBase courseBase = op.get();
        //根据mt和st的值去category表中查询对应id
        String mtId = courseBase.getMt();
        if (StringUtils.isNotEmpty(mtId)) {
            String mtName = courseCategoryMapper.findMtById(mtId);
            courseBase.setMt(mtName);
        }
        String stId = courseBase.getSt();
        if (StringUtils.isNotEmpty(stId)) {
            String stName = courseCategoryMapper.findStById(stId);
            courseBase.setSt(stName);
        }
        return courseBase;
    }

    /**
     * 更新课程信息
     *
     * @param id
     * @param courseBase
     * @return
     */
    @Transactional
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        //根据id查询是否有对应的课程
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (!optionalCourseBase.isPresent()) {
            //不存在添加课程
            this.addCourseBase(courseBase);
            return new ResponseResult(CourseCode.COURSE_IS_NULL);
        }
        //存在则覆盖（修改）注意这里走的不是save方法因为无法识别 mt和st 所以调用add方法 并且更新课程状态因为刚修改 所以置为未发布
        this.addCourseBase(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据课程id查询课程market
     *
     * @param courseid
     * @return
     */
    public CourseMarket getCourseMarketById(String courseid) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseid);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 修改课程营销计划
     *
     * @param id
     * @param courseMarket
     * @return
     */
    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(id);
        if (one != null) {
            //有则修改课程营销信息
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            //修改保存
            this.updateCourseStatus(id);
        } else {
            //无则，添加课程营销信息
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            //设置课程id
            one.setId(id);
            courseMarketRepository.save(one);
        }
        return one;
    }

    //抽取方法根据id修改课程状态信息
    public void updateCourseStatus(String id) {
        //修改课程状态为未发布
        //根据id查询课程
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            //如果存在对应的courseBase
            CourseBase courseBase = courseBaseOptional.get();
            // 状态设为待发布
            courseBase.setStatus("202001");
            //保存状态信息
            courseBaseRepository.save(courseBase);
        }
    }

    /**
     * 根据课程计划id修改课程计划nodename
     *
     * @param teachplanId
     * @param nodeName
     * @return
     */
    @Transactional
    public ResponseResult updateCoursePlan(String teachplanId, String nodeName) {

        //1.查询节点
        Optional<Teachplan> byId = teachplanRepository.findById(teachplanId);
        Teachplan teachplan = byId.get();

        //2.修改
        teachplan.setPname(nodeName);

        //3.保存
        teachplanRepository.save(teachplan);

        //4.返回成功
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * 根据课程计划id删除课程plan及以下的节点
     *
     * @param nodeId
     * @return
     */
    public ResponseResult deleteCourseNode(String nodeId) {
        return null;
    }

    /**
     * 根据课程id删除课程
     *
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCourse(String courseId) {
        //校验参数
        if (StringUtils.isEmpty(courseId)) {
            return new ResponseResult(CommonCode.FAIL);
        }
        try {
            //获取courseBase对应的mongdb 中的 cmspage
            CmsPage cmsPage = cmsPageClient.findByPageName(courseId + ".html");
            //删除cmspage中的cmspage对象
            cmsPageClient.delete(cmsPage.getPageId());
            //删除 mysql中的 coursebase表
            courseBaseRepository.deleteById(courseId);
            Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
            //有无图片 如果没有设置课程图片，则mysql、mongodb、fastdfs中都不会有图片的信
            if (coursePicOptional.isPresent()) {
                //删除mysql中的课程图片
                coursePicRepository.deleteById(courseId);
                //删除mongodb_fs中的图片信息
                String picPath = coursePicOptional.get().getPic();
                fileSystemRepository.deleteById(picPath);
                //远程调用fs微服务的接口删除服务器里的图片
                fileSystemClient.delete(picPath);
            }
            //course是否发布,如果发布需要删除服务器路径下对应的页面信息，还有coursepub表中信息
            Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
            if (coursePubOptional.isPresent()) {
                //删除发布表信息
                coursePubRepository.deleteById(courseId);
                //删除服务器下的html文件[暂时没写]和cmspage中保存的html文件
                cmsPageClient.deleteHtmlFile(cmsPage.getHtmlFileId());
            }
            //删除课程market
            //删除课程teachplan
            return new ResponseResult(CommonCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(FileSystemCode.FS_DELETEFILE_SERVERFAIL);
        }
    }

    //保存课程计划与媒资文件的关联信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //校验课程计划是否是3级
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询到课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if (mediaOptional.isPresent()) {
            one = mediaOptional.get();
        } else {
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }

}



















































