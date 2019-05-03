package net.simplyrin.discordvcrec.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SimplyRin on 2018/06/06.
 *
 * <!-- Apache v2.0 -->
 *  Copyright 2018 SimplyRin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ThreadPool {

	private static ExecutorService executorService = Executors.newFixedThreadPool(64);

	public static void run(Runnable runnable) {
		runAsync(runnable);
	}

	public static void runAsync(Runnable runnable) {
		executorService.execute(runnable);
	}

}
