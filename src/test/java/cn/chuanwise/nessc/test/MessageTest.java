package cn.chuanwise.nessc.test;

import cn.chuanwise.nessc.config.Messages;
import org.junit.jupiter.api.Test;

public class MessageTest {
    
    @Test
    void testSentence() {
        System.out.println(Messages.Sentence.of("Hey! {1} can be done!").format(new Throwable()));
    }
}
