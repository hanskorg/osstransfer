package org.hansk.tools;

import org.hansk.tools.transfer.Config;
import org.hansk.tools.transfer.action.FetchOssObjectRunner;
import org.hansk.tools.transfer.action.TransferRunner;
import javafx.scene.Parent;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

@SpringBootApplication
@MapperScan("io.pdtv.xingyan.transfer.dao")
public class OsstransferApplication {

	private static Logger logger = LoggerFactory.getLogger(OsstransferApplication.class);

	public static void main(String[] args) {

		ConfigurableApplicationContext configurableApplicationContext =  new SpringApplicationBuilder()
				.sources(Parent.class)
				.child(OsstransferApplication.class)
				.child(TransferRunner.class)
				.child(FetchOssObjectRunner.class)
				.run();
		configurableApplicationContext.getBean(Config.class).setStatus(Config.Status.STARTING);
		configurableApplicationContext.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {

			@Override
			public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
				OsstransferApplication.logger.info("shudowning");
				configurableApplicationContext.getBean(Config.class).setStatus(Config.Status.SHUTTING);
				configurableApplicationContext.getBean(TransferRunner.class).getScheduledThreadPoolExecutor().shutdown();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
