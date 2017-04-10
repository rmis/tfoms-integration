package ru.rtlabs;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.rtlabs.service.Service;

public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        Service service = ctx.getBean(Service.class);
        service.setId(Integer.parseInt(args[0]));
        service.initilize();
        service.postSend();
    }
}

