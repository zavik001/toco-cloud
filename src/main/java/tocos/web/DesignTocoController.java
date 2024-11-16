package tocos.web;

import jakarta.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import tocos.Ingredient;
import tocos.Ingredient.Type;
import tocos.Toco;
import tocos.TocoOrder;
import tocos.data.IngredientRepository;

@Slf4j
@Controller
@RequestMapping("/design")
@SessionAttributes("tacoOrder")
public class DesignTocoController {

    private final IngredientRepository ingredientRepo;

    @Autowired
    public DesignTocoController(IngredientRepository ingredientRepo) {
        this.ingredientRepo = ingredientRepo;
    }

    @ModelAttribute
    public void addIngredientsToModel(Model model) {

        Iterable<Ingredient> ingredients = ingredientRepo.findAll();
        /*
         * 
         * List<Ingredient> ingredients = Arrays.asList(
         * new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
         * new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
         * new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
         * new Ingredient("CARN", "Carnitas", Type.PROTEIN),
         * new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGIES),
         * new Ingredient("LETC", "Lettuce", Type.VEGGIES),
         * new Ingredient("CHED", "Cheddar", Type.CHEESE),
         * new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
         * new Ingredient("SLSA", "Salsa", Type.SAUSE),
         * new Ingredient("SRCR", "Sour Cream", Type.SAUSE));
         */

        Type[] types = Ingredient.Type.values();
        for (Type type : types) {
            model.addAttribute(type.toString().toLowerCase(),
                    filterbyType(ingredients, type));
        }
    }

    @ModelAttribute(name = "tacoOrder")
    public TocoOrder order() {
        return new TocoOrder();
    }

    @PostMapping
    public String processTaco(@Valid Toco toco,
            Errors errors,
            @ModelAttribute TocoOrder order) {
        if (errors.hasErrors())
            return "design";

        order.addTaco(toco);
        log.info("Processing toco: {}", toco);
        return "redirect:/orders/current";
    }

    @ModelAttribute(name = "toco")
    public Toco toco() {
        return new Toco();
    }

    @GetMapping
    public String showDesignform() {
        return "design";
    }

    private Iterable<Ingredient> filterbyType(
            Iterable<Ingredient> ingredients, Type type) {
        /*
         * return ingredients
         * .stream()
         * .filter(x -> x.getType().equals(type)).collect(Collectors.toList());
         */
        return StreamSupport.stream(ingredients.spliterator(), false)
                .filter(i -> i.getType().equals(type))
                .collect(Collectors.toList());
    }

}