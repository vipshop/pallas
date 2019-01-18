/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.utils;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CommonQueue<T> {

    private BlockingQueue<T> queue;

    public CommonQueue(int capacity){
        queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put(T e) throws InterruptedException {
        queue.put(e);
    }

    public void put(List<T> list) throws InterruptedException{
        for (T e: list){
            put(e);
        }
    }

    public void poll(List<T> list, int max){
        queue.drainTo(list, max); //NOSONAR
    }

    public T poll(){
        return queue.poll();
    }

    public int size(){
        return queue.size();
    }

    public void clear(){
        queue.clear();
    }
}