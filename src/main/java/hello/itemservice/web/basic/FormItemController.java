package hello.itemservice.web.basic;

import hello.itemservice.domain.item.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
public class FormItemController {

    private final ItemRepository itemRepository;

    /**
     * 성능 측면에서 static 클래스로 따로 분리 해보기!!
     * @return
     */
    //지역 데이터 생성
    @ModelAttribute("regions") //이 컨트롤러를 호출할 때 자동으로 이 ModelAttribute의 데이터를 모델에 담아준다.
    public Map<String, String> regions(){
        //지역 데이터를 넘기기 위한 map 생성
        Map<String, String> regions = new LinkedHashMap<>(); //순서를 보장하기 위해 LinkedHashMap 사용.
        regions.put("SEOUL","서울");
        regions.put("BUSAN","부산");
        regions.put("JEJU","제주");

        return regions; // model.addAttribute("regions", regions);
    }

    //상품 유형 데이터 생성
    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes(){

        return ItemType.values();// enum으로 생성한 value(Book, Food, ETC)가 배열로 넘어온다.
    }

    //배송 방식 데이터 생성
    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes(){
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NOMAL", "보통 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));

        return deliveryCodes;
    }

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
    public String viewAddForm(Model model){
        /*
        타임리프에서 th:object를 적용하려면 먼저 해당 오브젝트 정보를 넘겨주어야 한다.
        따라서, 빈 객체를 만들어서 뷰에 전달한다.
         */
        model.addAttribute("item", new Item()); // 빈 item 객체 저장.

        return "basic/addForm";
    }

    //상품 등록
    @PostMapping("/add")
    //@ModelAttribute의 name 속성을 생략했을 때 : 클래스명에서 첫글자만 소문자로 바꾼이름(Item -> item)으로 모델 객체를 만든다.
    //RedirectAttributes : 리다이렉트와 관련된 속성을 넣는 인터페이스
    public String save(@ModelAttribute("item") Item item, RedirectAttributes redirectAttributes){
        /*
        @ModelAttribute를 사용하면 아래의 모델 객체 생성 과정을 다 처리해준다.
         Item item = new Item();
         item.setItemName(itemName);
         item.setPrice(price);
         item.setQuantity(quantity);
        */

        log.info("item.open={}", item.getOpen());
        log.info("item.regions={}", item.getRegions());
        log.info("item.itemTypes={}", item.getItemType());

        Item savedItem = itemRepository.save(item);
        //model.addAttribute("item", item); //@ModelAttribute는 name 속성으로 지정한 이름("item")으로 모델에 객체를 저장한다.

        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); // 저장이 완료되었을 때 상태

        //return "basic/item"; //새로고침을 통해 바로 뷰 템플릿으로 넘어가면 계속 POST 호출이 되어 중복으로 등록되게 된다.

        //새로 고침 문제를 해결하기 위해 상품 저장 후에 상품 상세 화면으로 리다이렉트를 호출.(GET으로 재호출)
        /* PRG Post/Redirect/Get 해결 방식 */
        return "redirect:/basic/items/{itemId}";
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




}
