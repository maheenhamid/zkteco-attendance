package com.zkteco.attendance.second.common;

public class ItemResponse<T> extends BaseResponse {
    private T item;


    public ItemResponse() {
    }

    public ItemResponse(T item) {
        super();
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }
    

}
