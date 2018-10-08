package com.lib.bibliosoft.controller;

import com.lib.bibliosoft.DAO.IReaderDao;
import com.lib.bibliosoft.entity.Reader;
import com.lib.bibliosoft.repository.ReaderRepository;
import com.lib.bibliosoft.service.IReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: 毛文杰
 * @Description:
 * @Date: Created in 4:34 PM. 9/30/2018
 * @Modify By:
 */
@Controller
public class ReaderController {

    @Autowired
    private IReaderService iReaderService;

    @Autowired
    private IReaderDao iReaderDao;

    @Autowired
    private ReaderRepository readerRepository;

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(ReaderController.class);

    private Integer pagesize = 6;

    private Integer totalCount;


    /**
     * list all the reader
     * @param model
     * @return
     */
    @GetMapping("/reader_list")
    public String readerList(Model model){

        Integer currpage = 1;
        totalCount = iReaderDao.findAll().size();
        model.addAttribute("totalcount", totalCount);
        Integer totalPages = (totalCount + pagesize - 1)/pagesize;
        model.addAttribute("totalpages", totalPages);

        //获得每页的数据
        Iterator<Reader> readerIterator = iReaderService.getPage(currpage, pagesize).iterator();

        logger.info("currpage={}",currpage);
        List<Reader> list = new ArrayList<>();
        while(readerIterator.hasNext()) {
            list.add(readerIterator.next());
        }
        logger.info("list.size = {}",list.size());
        logger.info("list[0]={}", list.get(0));
        //放在model
        model.addAttribute("readers", list);
        model.addAttribute("currpage",currpage);
        return "reader_list";
    }

    /**
     * show detail information of a reader
     * @param readerId
     * @param model
     * @return
     */
    @GetMapping("/reader_show/{id}")
    public String show_reader(@PathVariable("id") String readerId, Model model){
        Integer readerid = Integer.parseInt(readerId);
        Reader reader = iReaderDao.findByReaderId(readerid);
        model.addAttribute("reader", reader);
        return "reader_show";
    }

    /**
     * librarian delete a reader
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String delete_reader(String id){
        Integer Iid = Integer.parseInt(id);
        Reader reader = iReaderDao.findById(Iid);
        iReaderDao.deleteReader(reader);
        logger.info("delete reader>>>  id={}",id);
        return "success";
    }

    /**
     * add/edit a reader in the layer
     * @param readerId
     * @param readerName
     * @param sex
     * @param phone
     * @param email
     * @return
     */
    @PostMapping("/add_reader")
    public String reader_add(Integer readerId,String readerName,
                             @RequestParam("form-field-radio") String sex,
                             String phone, String email,
                             @RequestParam("form-field-radio1") String status, String flag){
        if (flag.equals("edit")){
            Integer id = readerRepository.findReaderByReaderId(readerId).getId();
            iReaderDao.updateReader(id, sex, readerName, phone, readerId, email, status);
        }else if(flag.equals("add")){
            Reader reader = new Reader();
            reader.setSex(sex);
            reader.setReaderName(readerName);
            reader.setPhone(phone);
            reader.setReaderId(readerId);
            reader.setEmail(email);
            reader.setStatus(status);
            iReaderDao.addReader(reader);
            logger.info("Add reader={}", reader.toString());
        }
        return "redirect:/reader_list";
    }

    /**
     * modify reader's Status to OFF
     * @param id
     * @param model
     * @return
     */
    @PostMapping("/reader_stop")
    @ResponseBody
    public String stop_reader(String id, Model model){
        logger.info("id={}",id);
        Integer Iid = Integer.parseInt(id);
        logger.info("ON >>> OFF");
        iReaderDao.updateReaderStatusById(Iid, "OFF");
        logger.info("reader.status >>> ={}",iReaderDao.findById(Iid).getStatus());
        model.addAttribute("readers", iReaderDao.findAll());
        return Iid.toString();
    }

    /**
     * modify reader's Status to OFF
     * @param id
     * @param model
     * @return
     */
    @PostMapping("/reader_start")
    @ResponseBody
    public String start_reader(String id, Model model){
        Integer Iid = Integer.parseInt(id);
        logger.info("OFF >>> ON");
        iReaderDao.updateReaderStatusById(Iid, "ON");
        logger.info("reader.status >>> ={}",iReaderDao.findById(Iid).getStatus());
        model.addAttribute("readers", iReaderDao.findAll());
        return Iid.toString();
    }

    /**
     * show the reader pages by paging
     * @param currpage
     * @param model
     * @return
     */
    @GetMapping("/reader_page")
    public String page_reader(@RequestParam(value = "currpage") Integer currpage, Model model){

        totalCount = iReaderDao.findAll().size();
        model.addAttribute("totalcount", totalCount);
        Integer totalPages = (totalCount + pagesize - 1)/pagesize;
        model.addAttribute("totalpages", totalPages);

        if(currpage == 0)
            currpage = 1;
        if(currpage == totalPages+1)
            currpage = totalPages;
        //获得每页的数据
        Iterator<Reader> readerIterator = iReaderService.getPage(currpage, pagesize).iterator();

        logger.info("currpage={}",currpage);
        List<Reader> list = new ArrayList<>();
        while(readerIterator.hasNext()) {
            list.add(readerIterator.next());
        }
        logger.info("list.size = {}",list.size());
        //logger.info("list[0]={}", list.get(0));
        //放在model
        model.addAttribute("readers", list);
        model.addAttribute("currpage",currpage);
        return "reader_list";
    }

    // 排序分页显示数据
    @PostMapping("/reader_pageSort")
    @ResponseBody
    public Page<Reader> showSortPage(@RequestParam(value = "currpage") Integer currpage, @RequestParam(value = "pagesize") Integer pagesize){
        logger.info("paging-sort >>> currpage= {}, pagesize= {}", currpage, pagesize);
        return iReaderService.getPageSort(currpage, pagesize);
    }

    /**
     * search reader by Phone or Reader_name
     * @param model
     * @param search_content
     * @return
     */
    @RequestMapping("/reader_serach")
    public String search_reader(Model model, @RequestParam("search_content") String search_content){
        logger.info("search_content==={}",search_content);
        List<Reader> searchReader = iReaderService.searchReaderByPhoneOrName(search_content);

        logger.info("查询结果===大小size={}",searchReader.size());
        model.addAttribute("readers",searchReader);
        //当前页写死了
        model.addAttribute("currpage",1);
        model.addAttribute("totalcount", searchReader.size());
        Integer totalPages = (searchReader.size() + pagesize - 1)/pagesize;
        model.addAttribute("totalpages", totalPages);
        return "reader_list";
    }
}
