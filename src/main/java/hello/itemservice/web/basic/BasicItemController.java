package hello.itemservice.web.basic;

import hello.itemservice.domain.Item;
import hello.itemservice.domain.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@Controller
@RequestMapping("/basic/items")
/*
@RequiredArgsConstructor는 private final 키워드가 붙은 필드의 생성자를 만들어 준다.

    public BasicItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    => 생성자 주입 방식 (DI)
 */
@RequiredArgsConstructor
public class BasicItemController {

    private final ItemRepository itemRepository;

    //상품 목록 조회
    @GetMapping
    public String items(Model model){

        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);

        return "basic/items";
    }

    //상품 상세 조회
    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model){

        Item item = itemRepository.findById(itemId); //id 값으로 item 객체 생성.
        model.addAttribute("item", item); //모델에 item 객체 저장.

        return "basic/item";

    }

    //상품 등록 화면 출력
    @GetMapping("/add")
    public String viewAddForm(){
        return "basic/addForm";
    }

    //상품 등록
    @PostMapping("/add")
    //@ModelAttribute의 name 속성을 생략했을 때 : 클래스명에서 첫글자만 소문자로 바꾼이름(Item -> item)으로 모델 객체를 만든다.
    public String save(@ModelAttribute("item") Item item, Model model){
        /*
        @ModelAttribute를 사용하면 아래의 모델 객체 생성 과정을 다 처리해준다.
         Item item = new Item();
         item.setItemName(itemName);
         item.setPrice(price);
         item.setQuantity(quantity);
        */

        itemRepository.save(item);
        //model.addAttribute("item", item); //@ModelAttribute는 name 속성으로 지정한 이름("item")으로 모델에 객체를 저장한다.

        //return "basic/item"; //새로고침을 통해 바로 뷰 템플릿으로 넘어가면 계속 POST 호출이 되어 중복으로 등록되게 된다.

        //새로 고침 문제를 해결하기 위해 상품 저장 후에 뷰 템플릿으로 이동하는 것이 아니라, 상품 상세 화면으로 리다이렉트를 호출.
        return "redirect:/basic/items" + item.getId();
    }

    //상품 수정 화면 출력
    @GetMapping("/{itemId}/edit") // GET 경로에 있는 변수명과 @PathVariable의 변수명이 같아야 함.
    public String viewEditForm(@PathVariable Long itemId, Model model){

        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "basic/editForm";
    }

    //상품 수정
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable long itemId, @ModelAttribute Item editItem){

        itemRepository.update(itemId, editItem);

        //redirect를 하면 /basic/items/{itemId}/edit 경로가 아닌 아래의 경로로 다시 재요청된다.
        return "redirect:/basic/items/{itemId}"; //{itemId}값의 @PathVariable 값으로 들어간다.

    }



    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("testA", 10000, 10));
        itemRepository.save(new Item("testB", 20000, 20));
    }
}
