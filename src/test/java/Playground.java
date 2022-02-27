import lombok.SneakyThrows;
import net.eltown.tinyrabbit.TinyRabbit;
import net.eltown.tinyrabbit.TinyRabbitListener;
import net.eltown.tinyrabbit.handler.CallbackKeyHandler;
import net.eltown.tinyrabbit.handler.KeyHandler;

public class Playground {

    @SneakyThrows
    public static void main(String[] args) {
        final TinyRabbitListener listener = new TinyRabbitListener("localhost");
        final TinyRabbit tinyRabbit = new TinyRabbit("localhost", "Playground/Client");

        final CallbackKeyHandler callbackKeyHandler = new CallbackKeyHandler.Builder()
                .on("MY_KEY", (data, request) -> {
                    request.answer("MY_CALLBACK1", "MESSAGE");
                })
                .on("MY_KEY2", (data, request) -> {
                    request.answer("MY_CALLBACK2", "MESSAGE");
                })
                .onError(() -> {
                    System.out.println("Oh... Ein Fehler :(");
                })
                .build();

        listener.callback(callbackKeyHandler, "Playground/Callback", "playground.callback");

        final KeyHandler keyHandler = new KeyHandler.Builder()
                .on("MY_KEY1", (delivery) -> {

                })
                .build();

        tinyRabbit.sendAndReceive(keyHandler, "playground.callback", "MY_KEY", "Hello!");
    }

}
