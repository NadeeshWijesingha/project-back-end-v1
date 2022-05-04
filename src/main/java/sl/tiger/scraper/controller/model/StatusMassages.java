package sl.tiger.scraper.controller.model;

public enum StatusMassages {
    LOGIN_SUCCESS("Login Success"),
    SELECT_YEAR_FAILED("Invalid Year Input"),
    SELECT_MAKE_FAILED("Invalid Make Input"),
    SELECT_MODEL_FAILED("Invalid Model Input"),
    SELECT_ENGINE_FAILED("Invalid Engine Input"),
    SELECT_CATEGORY_FAILED("Invalid Category Input"),
    SELECT_GROUP_FAILED("Invalid Group Input"),
    SELECT_YEAR_SUCCESS("Select Year Success"),
    SELECT_MAKE_SUCCESS("Select Make Success"),
    SELECT_MODEL_SUCCESS("Select Model Success"),
    SELECT_ENGINE_SUCCESS("Select Engine Success"),
    SELECT_CATEGORY_SUCCESS("Select Category Success"),
    SELECT_GROUP_SUCCESS("Select Group Success"),
    SOMETHING_WENT_WRONG("Something Went Wrong.!"),
    PART_NOT_AVAILABLE("Part You Searched Not Available"),
    PART_NO_SEARCH_SUCCESS("Part Nu Search Success"),
    ADD_TO_CART_SUCCESS("Add To Cart Success"),
    ADD_TO_CART_FAILED("Add To Cart Failed"),
    PAGE_RESET_SUCCESS("Page Reset Success"),
    TEXT_SEARCH_SUCCESS("Text Search Success"),
    SET_RESULT_SUCCESS("Set Result Success"),
    GET_SCREENSHOT_SUCCESS("Get Screenshot Success"),
    GET_SCREENSHOT_FAILED("Get Screenshot Failed");

    public final String status;

    StatusMassages(String status) {
        this.status = status;
    }
}
