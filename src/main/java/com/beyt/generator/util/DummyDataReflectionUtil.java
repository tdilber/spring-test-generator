package com.beyt.generator.util;

import com.beyt.generator.exception.DummyDataFileReadException;
import com.beyt.generator.util.field.FieldUtil;
import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.validation.constraints.Size;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by tdilber at 27-Aug-19
 */
@Slf4j
public final class DummyDataReflectionUtil {
    private static Random random = new Random();

    private static final Faker faker;
    private static final List<String> fakerResolveList;
    private static final FakeValuesService fakeValuesService;

    public static void fillParametersRandom(Object object, Class<?> clazz, int index, List<ReflectionUtil.TwoGenericTypeResult<JpaRepository<?, ?>>> repositoryMap, Map<String, String> fieldFieldValueMap) {
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method method = property.getWriteMethod();
                String fieldName = property.getName();

                if (method != null) {
                    Object field = null;

                    if (fieldFieldValueMap.containsKey(fieldName.toLowerCase())) {
                        try {
                            field = FieldUtil.fillValue(method.getParameterTypes()[0], DummyDataStringUtil.processFieldValue(fieldFieldValueMap.get(fieldName.toLowerCase())));
                        } catch (Exception e) {
                            throw new DummyDataFileReadException("Invalid Value(" + fieldFieldValueMap.get(fieldName.toLowerCase()) + ") for Field Type!(" + method.getParameterTypes()[0].getSimpleName() + ")", e);
                        }
                    }


                    if (field == null && method.getParameterTypes()[0].equals(String.class)) {
                        field = fillWithFaker(fieldName);
                    }

                    if (field == null) {
                        try {
                            field = FieldUtil.fillRandom(method.getParameterTypes()[0]);
                        } catch (IllegalStateException e) {
                            log.warn(e.getMessage());
                        }
                    }

                    if (method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                        AtomicReference<Integer> size = new AtomicReference<>();
                        Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getName().equals(property.getName())).findFirst().ifPresent(fieldClazz -> {
                            Column columnAnnotation = fieldClazz.getAnnotation(Column.class);
                            if (columnAnnotation != null) {
                                size.set(columnAnnotation.length());
                            }

                            Size sizeAnnotation = fieldClazz.getAnnotation(Size.class);
                            if (sizeAnnotation != null) {
                                size.set(sizeAnnotation.max());
                            }
                        });

                        String strResult = (String) field;

                        if (size.get() != null && strResult.length() > size.get()) {
                            strResult = strResult.substring(strResult.length() - size.get());
                        }

                        field = strResult;
                    }

                    if (field == null) {
                        JpaRepository<?, ?> repository = ReflectionUtil.findObjectWithType1(method.getParameterTypes()[0], repositoryMap);
                        if (repository != null) {
                            Long count = repository.count();
                            if (count > 0) {
                                int ordinal = random.nextInt(count.intValue());
                                Page questionPage = ((JpaSpecificationExecutor<?>) repository).findAll(null, PageRequest.of(ordinal, 1, Sort.Direction.ASC, "id"));
                                if (questionPage.hasContent()) {
                                    field = questionPage.getContent().get(0);
                                }
                            } else {
                                log.warn(method.getParameterTypes()[0].getSimpleName() + " entity not found for " + clazz.getSimpleName() + " Repository!");
                            }
                        }
                    }
                    // For SQL ID sequence problem
                    if (fieldName.equals("id") && Number.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        field = null;
                    }

                    if (field != null) {
                        method.invoke(object, field);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nullable
    public static String fillWithFaker(String fieldName) {
        return resolve(getMostMatchedFakerResolve(fieldName));
    }

    @Nullable
    public static String getMostMatchedFakerResolve(String fieldName) {
        List<String> matchList = new ArrayList<>();
        fieldName = fieldName.toLowerCase();

        for (String fakerResolve : fakerResolveList) {
            if (fakerResolve.contains(fieldName)) {
                matchList.add(fakerResolve);
            }
        }

        if (matchList.isEmpty()) {
            return null;
        } else if (matchList.size() == 1) {
            return matchList.get(0);
        } else {
            return matchList.get(getBiggestScoreIndex(fieldName, matchList));
        }
    }

    @Nullable
    private static String resolve(String key) {
        String result;

        if (key == null) {
            return null;
        } else if (key.startsWith("address.")) {
            result = fakeValuesService.resolve(key, faker.address(), faker);
        } else if (key.startsWith("team.")) {
            result = fakeValuesService.resolve(key, faker.team(), faker);
        } else if (key.startsWith("job.")) {
            result = fakeValuesService.resolve(key, faker.job(), faker);
        } else if (key.startsWith("name.")) {
            result = fakeValuesService.resolve(key, faker.name(), faker);
        } else if (key.startsWith("company.")) {
            result = fakeValuesService.resolve(key, faker.company(), faker);
        } else {
            result = faker.resolve(key);
        }

        return result;
    }

    @NonNull
    private static int getBiggestScoreIndex(String fieldName, List<String> matchList) {
        int biggestScoreIndex = 0;
        int biggestScore = 0;

        for (int i = 0; i < matchList.size(); i++) {
            int matchScore = 0;
            for (String subMatch : matchList.get(i).split("\\.")) {
                if (subMatch.equals(fieldName)) {
                    matchScore += 3;
                } else if (subMatch.contains(fieldName)) {
                    matchScore += 1;
                }
            }

            if (matchScore > biggestScore) {
                biggestScore = matchScore;
                biggestScoreIndex = i;
            }
        }
        return biggestScoreIndex;
    }

    static {
        RandomService randomService = new RandomService();
        fakeValuesService = new FakeValuesService(new Locale("en-US"), randomService);
        faker = new Faker(fakeValuesService, randomService);
        fakerResolveList = new ArrayList<>();
        fakerResolveList.add("address.street_name");
        fakerResolveList.add("address.street_address");
        fakerResolveList.add("address.secondary_address");
        fakerResolveList.add("address.postcode");
        fakerResolveList.add("address.street_suffix");
        fakerResolveList.add("address.street_prefix");
        fakerResolveList.add("address.city_suffix");
        fakerResolveList.add("address.city_prefix");
        fakerResolveList.add("address.city");
        fakerResolveList.add("address.city_name");
        fakerResolveList.add("address.state");
        fakerResolveList.add("address.state_abbr");
        fakerResolveList.add("address.time_zone");
        fakerResolveList.add("address.country");
        fakerResolveList.add("address.country_code");
        fakerResolveList.add("address.building_number");
        fakerResolveList.add("address.full_address");
        fakerResolveList.add("ancient.god");
        fakerResolveList.add("ancient.primordial");
        fakerResolveList.add("ancient.titan");
        fakerResolveList.add("ancient.hero");
        fakerResolveList.add("creature.animal.name");
        fakerResolveList.add("app.name");
        fakerResolveList.add("app.version");
        fakerResolveList.add("app.author");
        fakerResolveList.add("aqua_teen_hunger_force.character");
        fakerResolveList.add("internet.avatar");
        fakerResolveList.add("back_to_the_future.characters");
        fakerResolveList.add("back_to_the_future.dates");
        fakerResolveList.add("back_to_the_future.quotes");
        fakerResolveList.add("beer.name");
        fakerResolveList.add("beer.style");
        fakerResolveList.add("beer.hop");
        fakerResolveList.add("beer.yeast");
        fakerResolveList.add("beer.malt");
        fakerResolveList.add("bojack_horseman.characters");
        fakerResolveList.add("bojack_horseman.quotes");
        fakerResolveList.add("bojack_horseman.tongue_twisters");
        fakerResolveList.add("book.author");
        fakerResolveList.add("book.title");
        fakerResolveList.add("book.publisher");
        fakerResolveList.add("book.genre");
        fakerResolveList.add("buffy.characters");
        fakerResolveList.add("buffy.quotes");
        fakerResolveList.add("buffy.celebrities");
        fakerResolveList.add("buffy.big_bads");
        fakerResolveList.add("buffy.episodes");
        fakerResolveList.add("business.credit_card_numbers");
        fakerResolveList.add("business.credit_card_types");
        fakerResolveList.add("business.credit_card_expiry_dates");
        fakerResolveList.add("creature.cat.name");
        fakerResolveList.add("creature.cat.breed");
        fakerResolveList.add("creature.cat.registry");
        fakerResolveList.add("chuck_norris.fact");
        fakerResolveList.add("code.asin");
        fakerResolveList.add("coin.flip");
        fakerResolveList.add("color.name");
        fakerResolveList.add("color.name");
        fakerResolveList.add("commerce.department");
        fakerResolveList.add("commerce.product_name.adjective");
        fakerResolveList.add("commerce.product_name.material");
        fakerResolveList.add("commerce.product_name.product");
        fakerResolveList.add("commerce.product_name.material");
        fakerResolveList.add("commerce.promotion_code.adjective");
        fakerResolveList.add("commerce.promotion_code.noun");
        fakerResolveList.add("company.name");
        fakerResolveList.add("company.suffix");
        fakerResolveList.add("company.industry");
        fakerResolveList.add("company.profession");
        fakerResolveList.add("internet.domain_suffix");
        fakerResolveList.add("country.code2");
        fakerResolveList.add("country.code3");
        fakerResolveList.add("country.capital");
        fakerResolveList.add("country.currency");
        fakerResolveList.add("country.currency_code");
        fakerResolveList.add("country.name");
        fakerResolveList.add("currency.name");
        fakerResolveList.add("currency.code");
        fakerResolveList.add("creature.dog.name");
        fakerResolveList.add("creature.dog.breed");
        fakerResolveList.add("creature.dog.sound");
        fakerResolveList.add("creature.dog.meme_phrase");
        fakerResolveList.add("creature.dog.age");
        fakerResolveList.add("creature.dog.coat_length");
        fakerResolveList.add("creature.dog.gender");
        fakerResolveList.add("creature.dog.size");
        fakerResolveList.add("dragon_ball.characters");
        fakerResolveList.add("dune.characters");
        fakerResolveList.add("dune.titles");
        fakerResolveList.add("dune.planets");
        fakerResolveList.add("educator.name");
        fakerResolveList.add("educator.tertiary.degree.type");
        fakerResolveList.add("educator.name");
        fakerResolveList.add("educator.name");
        fakerResolveList.add("games.elder_scrolls.race");
        fakerResolveList.add("games.elder_scrolls.creature");
        fakerResolveList.add("games.elder_scrolls.region");
        fakerResolveList.add("games.elder_scrolls.dragon");
        fakerResolveList.add("games.elder_scrolls.city");
        fakerResolveList.add("games.elder_scrolls.first_name");
        fakerResolveList.add("games.elder_scrolls.last_name");
        fakerResolveList.add("games.elder_scrolls.quote");
        fakerResolveList.add("esport.players");
        fakerResolveList.add("esport.teams");
        fakerResolveList.add("esport.events");
        fakerResolveList.add("esport.leagues");
        fakerResolveList.add("esport.games");
        fakerResolveList.add("file.extension");
        fakerResolveList.add("file.mime_type");
        fakerResolveList.add("food.ingredients");
        fakerResolveList.add("food.spices");
        fakerResolveList.add("food.dish");
        fakerResolveList.add("food.fruits");
        fakerResolveList.add("food.vegetables");
        fakerResolveList.add("food.sushi");
        fakerResolveList.add("food.measurement_sizes");
        fakerResolveList.add("friends.characters");
        fakerResolveList.add("friends.locations");
        fakerResolveList.add("friends.quotes");
        fakerResolveList.add("funny_name.name");
        fakerResolveList.add("game_of_thrones.characters");
        fakerResolveList.add("game_of_thrones.houses");
        fakerResolveList.add("game_of_thrones.cities");
        fakerResolveList.add("game_of_thrones.dragons");
        fakerResolveList.add("game_of_thrones.quotes");
        fakerResolveList.add("hacker.abbreviation");
        fakerResolveList.add("hacker.adjective");
        fakerResolveList.add("hacker.noun");
        fakerResolveList.add("hacker.verb");
        fakerResolveList.add("hacker.ingverb");
        fakerResolveList.add("harry_potter.characters");
        fakerResolveList.add("harry_potter.locations");
        fakerResolveList.add("harry_potter.quotes");
        fakerResolveList.add("harry_potter.books");
        fakerResolveList.add("harry_potter.houses");
        fakerResolveList.add("harry_potter.spells");
        fakerResolveList.add("hipster.words");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.characters");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.locations");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.marvin_quote");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.planets");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.quotes");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.species");
        fakerResolveList.add("hitchhikers_guide_to_the_galaxy.starships");
        fakerResolveList.add("hobbit.character");
        fakerResolveList.add("hobbit.thorins_company");
        fakerResolveList.add("hobbit.quote");
        fakerResolveList.add("hobbit.location");
        fakerResolveList.add("how_i_met_your_mother.character");
        fakerResolveList.add("how_i_met_your_mother.catch_phrase");
        fakerResolveList.add("how_i_met_your_mother.high_five");
        fakerResolveList.add("how_i_met_your_mother.quote");
        fakerResolveList.add("id_number.valid");
        fakerResolveList.add("id_number.invalid");
        fakerResolveList.add("internet.free_email");
        fakerResolveList.add("internet.safe_email");
        fakerResolveList.add("internet.domain_suffix");
        fakerResolveList.add("internet.image_dimension");
        fakerResolveList.add("job.field");
        fakerResolveList.add("job.seniority");
        fakerResolveList.add("job.position");
        fakerResolveList.add("job.key_skills");
        fakerResolveList.add("job.title");
        fakerResolveList.add("games.league_of_legends.champion");
        fakerResolveList.add("games.league_of_legends.location");
        fakerResolveList.add("games.league_of_legends.quote");
        fakerResolveList.add("games.league_of_legends.summoner_spell");
        fakerResolveList.add("games.league_of_legends.masteries");
        fakerResolveList.add("games.league_of_legends.rank");
        fakerResolveList.add("lebowski.actors");
        fakerResolveList.add("lebowski.characters");
        fakerResolveList.add("lebowski.quotes");
        fakerResolveList.add("lord_of_the_rings.characters");
        fakerResolveList.add("lord_of_the_rings.locations");
        fakerResolveList.add("lorem.words");
        fakerResolveList.add("matz.quotes");
        fakerResolveList.add("medical.medicine_name");
        fakerResolveList.add("medical.disease_name");
        fakerResolveList.add("medical.hospital_name");
        fakerResolveList.add("medical.symptoms");
        fakerResolveList.add("music.instruments");
        fakerResolveList.add("music.genres");
        fakerResolveList.add("name.name");
        fakerResolveList.add("name.name_with_middle");
        fakerResolveList.add("name.first_name");
        fakerResolveList.add("name.last_name");
        fakerResolveList.add("name.prefix");
        fakerResolveList.add("name.suffix");
        fakerResolveList.add("name.title.descriptor");
        fakerResolveList.add("name.title.level");
        fakerResolveList.add("name.title.job");
        fakerResolveList.add("name.blood_group");
        fakerResolveList.add("nation.nationality");
        fakerResolveList.add("nation.language");
        fakerResolveList.add("nation.capital_city");
        fakerResolveList.add("games.overwatch.heroes");
        fakerResolveList.add("games.overwatch.locations");
        fakerResolveList.add("games.overwatch.quotes");
        fakerResolveList.add("cell_phone.formats");
        fakerResolveList.add("phone_number.formats");
        fakerResolveList.add("games.pokemon.names");
        fakerResolveList.add("games.pokemon.locations");
        fakerResolveList.add("princess_bride.characters");
        fakerResolveList.add("princess_bride.quotes");
        fakerResolveList.add("programming_language.name");
        fakerResolveList.add("programming_language.creator");
        fakerResolveList.add("relationship.familial.direct");
        fakerResolveList.add("relationship.familial.extended");
        fakerResolveList.add("relationship.in_law");
        fakerResolveList.add("relationship.spouse");
        fakerResolveList.add("relationship.parent");
        fakerResolveList.add("relationship.sibling");
        fakerResolveList.add("rick_and_morty.characters");
        fakerResolveList.add("rick_and_morty.locations");
        fakerResolveList.add("rick_and_morty.quotes");
        fakerResolveList.add("robin.quotes");
        fakerResolveList.add("rock_band.name");
        fakerResolveList.add("slack_emoji.people");
        fakerResolveList.add("slack_emoji.nature");
        fakerResolveList.add("slack_emoji.food_and_drink");
        fakerResolveList.add("slack_emoji.celebration");
        fakerResolveList.add("slack_emoji.activity");
        fakerResolveList.add("slack_emoji.travel_and_places");
        fakerResolveList.add("slack_emoji.objects_and_symbols");
        fakerResolveList.add("slack_emoji.custom");
        fakerResolveList.add("space.planet");
        fakerResolveList.add("space.moon");
        fakerResolveList.add("space.galaxy");
        fakerResolveList.add("space.nebula");
        fakerResolveList.add("space.star_cluster");
        fakerResolveList.add("space.constellation");
        fakerResolveList.add("space.star");
        fakerResolveList.add("space.agency");
        fakerResolveList.add("space.agency_abv");
        fakerResolveList.add("space.nasa_space_craft");
        fakerResolveList.add("space.company");
        fakerResolveList.add("space.distance_measurement");
        fakerResolveList.add("space.meteorite");
        fakerResolveList.add("star_trek.character");
        fakerResolveList.add("star_trek.location");
        fakerResolveList.add("star_trek.specie");
        fakerResolveList.add("star_trek.villain");
        fakerResolveList.add("stock.symbol_nsdq");
        fakerResolveList.add("stock.symbol_nyse");
        fakerResolveList.add("superhero.name");
        fakerResolveList.add("superhero.prefix");
        fakerResolveList.add("superhero.suffix");
        fakerResolveList.add("superhero.power");
        fakerResolveList.add("superhero.descriptor");
        fakerResolveList.add("team.name");
        fakerResolveList.add("team.creature");
        fakerResolveList.add("address.state");
        fakerResolveList.add("team.sport");
        fakerResolveList.add("twin_peaks.characters");
        fakerResolveList.add("twin_peaks.locations");
        fakerResolveList.add("twin_peaks.quotes");
        fakerResolveList.add("university.name");
        fakerResolveList.add("university.prefix");
        fakerResolveList.add("university.suffix");
        fakerResolveList.add("weather.description");
        fakerResolveList.add("games.witcher.characters");
        fakerResolveList.add("games.witcher.witchers");
        fakerResolveList.add("games.witcher.schools");
        fakerResolveList.add("games.witcher.locations");
        fakerResolveList.add("games.witcher.quotes");
        fakerResolveList.add("games.witcher.monsters");
        fakerResolveList.add("yoda.quotes");
        fakerResolveList.add("games.zelda.games");
        fakerResolveList.add("games.zelda.characters");
        fakerResolveList.add("address.city_prefix");
        fakerResolveList.add("internet.safe_email");
        fakerResolveList.add("internet.safe_email");
    }
}
