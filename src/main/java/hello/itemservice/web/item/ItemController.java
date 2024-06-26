package hello.itemservice.web.item;

import hello.itemservice.domain.file.FileStore;
import hello.itemservice.domain.item.*;
import hello.itemservice.web.item.form.ItemSaveDto;
import hello.itemservice.web.item.form.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Version 3 : 검증 방식을 Bean Validation으로 수정.
 */
@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    /**
     * 성능 측면에서 static 클래스로 따로 분리 해보기!!
     * @return
     */
    //지역 데이터 생성
    @ModelAttribute("regions") //이 컨트롤러를 호출할 때 자동으로 이 ModelAttribute의 데이터를 모델에 담아준다.
    public Map<String, String> regions(){
        //지역 데이터를 넘기기 위한 map 생성
        Map<String, String> regions = new LinkedHashMap<>(); //순서를 보장하기 위해 LinkedHashMap 사용.
        regions.put("SEOUL","서울(SEOUL)");
        regions.put("BUSAN","부산(BUSAN)");
        regions.put("JEJU","제주(JEJU)");

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
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송(Fast)"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "보통 배송(Normal)"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송(Slow)"));

        return deliveryCodes;
    }

    //상품 목록 조회
    @GetMapping
    public String items(Model model){

        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);

        return "items/items";
    }

    //상품 상세 조회
    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model){

        log.info("itemId = {}", itemId);

        Item item = itemRepository.findById(itemId); //id 값으로 item 객체 생성.
        model.addAttribute("item", item); //모델에 item 객체 저장.

        return "items/item";

    }

    //상품 등록 화면 출력
    @GetMapping("/add")
    public String viewAddForm(Model model){
        /*
        타임리프에서 th:object를 적용하려면 먼저 해당 오브젝트 정보를 넘겨주어야 한다.
        따라서, 빈 객체를 만들어서 뷰에 전달한다.
         */
        model.addAttribute("item", new Item()); // 빈 item 객체 저장.

        return "items/addForm";
    }

    //상품 등록
    //BeanValidation groups(SaveCheck) : 등록 검증만 적용.
    @PostMapping("/add")
    public String save(@Validated @ModelAttribute("item") ItemSaveDto itemSaveDto, BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {

        //검증에 실패 -> 다시 입력 폼으로 이동.
        if(bindingResult.hasErrors()){
            log.info("errors = {} ", bindingResult);
            return "items/addForm";
        }

        //Bean Validation- ObjectError 처리 방법 2 (직접 자바 코드로 작성 - 권장 O)
        //특정 필드가 아닌 복합 룰 검증(global errors)
        //가격과 수량이 null이 아니고 가격*수량이 10000원 이하일 때 (ObjectError)
        if(itemSaveDto.getPrice()!=null && itemSaveDto.getQuantity()!=null){
            int resultPrice = itemSaveDto.getPrice() * itemSaveDto.getQuantity();
            if(resultPrice < 10000){
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }


        //검증 성공 로직
        //업로드 파일(상품이미지) 저장
        List<UploadFile> imageFiles = fileStore.storeFiles(itemSaveDto.getImageFiles());

        //Dto를 가지고 item 객체 생성
        Item item = new Item();
        item.setItemName(itemSaveDto.getItemName());
        item.setPrice(itemSaveDto.getPrice());
        item.setQuantity(itemSaveDto.getQuantity());
        item.setImageFiles(imageFiles);

        Item savedItem = itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true); // 저장이 완료되었을 때 상태

        log.info("item = {} ", savedItem);
        //새로 고침 문제를 해결하기 위해 상품 저장 후에 상품 상세 화면으로 리다이렉트를 호출.(GET으로 재호출)
        /* PRG Post/Redirect/Get 해결 방식 */
        return "redirect:/items/{itemId}";
    }

    //서버에 저장된 이미지 불러오기
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        log.info("filename={}",filename);
        //"file:/Users/../uuid파일명"
        //file: ~로 시작하게 되면 이 파일에 직접 접근하여 리소스를 가져오고 스트림이 되어 화면으로 뿌려주게 된다.
        return new UrlResource("file:" + fileStore.getFullPath(filename));

        //-> 이 메서드는 보안에 취약하므로 체크 로직을 추가하여 보안을 강화해야 함.
    }


    //상품 수정 화면 출력
    @GetMapping("/{itemId}/edit") // GET 경로에 있는 변수명과 @PathVariable의 변수명이 같아야 함.
    public String viewEditForm(@PathVariable Long itemId, Model model){

        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "items/editForm";
    }

    //상품 수정
    //BeanValidation groups(UpdateCheck) : 수정 검증만 적용.
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable long itemId, @Validated @ModelAttribute("item") ItemUpdateDto itemUpdateDto, BindingResult bindingResult){

        //Bean Validation- ObjectError 처리 방법 2 (직접 자바 코드로 작성 - 권장 O)
        //특정 필드가 아닌 복합 룰 검증(global errors)
        //가격과 수량이 null이 아니고 가격*수량이 10000원 이하일 때 (ObjectError)
        if(itemUpdateDto.getPrice()!=null && itemUpdateDto.getQuantity()!=null){
            int resultPrice = itemUpdateDto.getPrice() * itemUpdateDto.getQuantity();
            if(resultPrice < 10000){
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //검증에 실패 -> 다시 입력 폼으로 이동.
        if(bindingResult.hasErrors()){
            log.info("errors = {} ", bindingResult);
            return "items/editForm";
        }

        //Dto를 가지고 item 객체 생성
        Item item = new Item();
        item.setItemName(itemUpdateDto.getItemName());
        item.setPrice(itemUpdateDto.getPrice());
        item.setQuantity(itemUpdateDto.getQuantity());


        itemRepository.update(itemId, item);

        //redirect를 하면 /items/items/{itemId}/edit 경로가 아닌 아래의 경로로 다시 재요청된다.
        return "redirect:/items/{itemId}"; //{itemId}값의 @PathVariable 값으로 들어간다.

    }




}
