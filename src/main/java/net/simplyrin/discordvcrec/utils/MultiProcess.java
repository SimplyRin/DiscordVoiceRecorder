package net.simplyrin.discordvcrec.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SimplyRin on 2019/04/11.
 *
 *  Copyright 2019 SimplyRin
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
public class MultiProcess {

	private ExecutorService executorService = Executors.newFixedThreadPool(16);
	private List<Runnable> runnables = new ArrayList<>();

	private boolean isRunnning;
	private Runnable finishedTask;

	public MultiProcess addProcess(final Runnable runnable) {
		this.runnables.add(runnable);
		return this;
	}

	public void updateMaxThread(int integer) {
		if (this.isRunnning) {
			System.err.println("The number of threads can not be changed during processing!");
			return;
		}
		this.executorService = Executors.newFixedThreadPool(integer);
	}

	public void start() {
		this.isRunnning = true;

		List<Runnable> tempRunnable = new ArrayList<>();
		tempRunnable.addAll(this.runnables);
		for (Runnable runnable : tempRunnable) {
			this.executorService.execute(() -> {
				try {
					runnable.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.remove(0);

				// System.out.println("Size: " + this.runnables.size());
				if (this.getRunnablesSize() == 0) {
					this.finish();
				}
			});
		}

		if (this.getRunnablesSize() == 0) {
			this.finish();
		}
	}

	private synchronized void remove(int integer) {
		this.runnables.remove(integer);
	}

	private synchronized int getRunnablesSize() {
		return this.runnables.size();
	}

	public void setFinishedTask(Runnable runnable) {
		this.finishedTask = runnable;
	}

	private boolean finished;

	private void finish() {
		if (this.finished) {
			return;
		}
		this.finished = true;
		this.isRunnning = false;

		this.finishedTask.run();
	}

}
