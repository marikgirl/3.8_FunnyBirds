package ru.samsung.itschool.funnybirds;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

public class DrawThread extends Thread {

    private Bitmap pauseBitmap;
    private boolean isPaused;
    private SurfaceHolder surfaceHolder;
    private volatile boolean running = true;

    private Sprite playerBird;
    private Sprite enemyBird;
    private Sprite touchBird;
    private Sprite Bonus;

    private DrawThread.Timer timer;

    private final int timerInterval = 30;

    private int points = 0;
    private int level = 1;

    private int pauseX;
    private int pauseY;

    private int viewWidth;
    private int viewHeight;

    private Context context;

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update ();
        }

        @Override
        public void onFinish() {
        }
    }

    public DrawThread(Context context, SurfaceHolder surfaceHolder) {
        this.context = context;
        //создаем холдер
        this.surfaceHolder = surfaceHolder;
        //создание спрайта и добавление в него кадров
        Bitmap playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        int playerWidth = playerBitmap.getWidth()/5;
        int playerHeight = playerBitmap.getHeight()/3;
        Rect firstFrame = new Rect(0, 0, playerWidth, playerHeight);
        playerBird = new Sprite(10, 0, 0, 400, firstFrame, playerBitmap);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j * playerWidth, i * playerHeight, j* playerWidth + playerWidth, i * playerWidth + playerWidth));
            }
        }
        Bitmap enemyBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);
        int enemyWidth = enemyBitmap.getWidth() / 5;
        int enemyHeight = enemyBitmap.getHeight() / 3;
        firstFrame = new Rect(4 * enemyWidth, 0, 5 * enemyWidth, enemyHeight);

        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, enemyBitmap);
        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue;
                }
                if (i ==2 && j == 0) {
                    continue;
                }
                enemyBird.addFrame(new Rect(j * enemyWidth, i * enemyHeight, j * enemyWidth + enemyWidth, i * enemyWidth + enemyWidth));
            }
        }

        Bitmap touchBirdBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.touchbird);
        int touchBirdWidth = touchBirdBitmap.getWidth()/5;
        int touchBirdHeight = touchBirdBitmap.getHeight()/3;
        firstFrame = new Rect(0, 0, touchBirdWidth, touchBirdHeight);
        touchBird = new Sprite(2000, 400, -300, 0, firstFrame, touchBirdBitmap);
        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue; }
                if (i ==2 && j == 0) {
                    continue; }
                touchBird.addFrame(new Rect(j * touchBirdWidth + 10, i * touchBirdHeight +10, j * touchBirdWidth + touchBirdWidth + 10, i * touchBirdWidth + touchBirdWidth));
            }
        }

        //бонус
        Bitmap bonusBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bonus);
        bonusBitmap = Bitmap.createScaledBitmap(bonusBitmap,140,140,true);
        int bonusWidth = bonusBitmap.getWidth();
        int bonusHeight = bonusBitmap.getHeight();
        firstFrame = new Rect(0,0, bonusWidth, bonusHeight );
        Bonus = new Sprite(2000,500,-150,0, firstFrame, bonusBitmap);

        isPaused = false;
        pauseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        pauseBitmap = Bitmap.createScaledBitmap(pauseBitmap,130,130,true);
        
        timer = new DrawThread.Timer();
        timer.start();
    }

    //метод для смены флага остановки треда
    public void requestStop() {
        running = false;
    }

    //onDraw в фоновом треде
    @Override
    public void run() {
        Canvas canvas;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(45.0f);
        p.setColor(Color.BLACK);

        while (running) {
            //блокировка холдера перед перерисовкой
            canvas = surfaceHolder.lockCanvas();
            viewWidth = canvas.getWidth();
            viewHeight = canvas.getHeight();
            try {
                //прорисовка фона и созданных объектов
                canvas.drawARGB(250, 127, 199, 255);
                playerBird.draw(canvas);
                enemyBird.draw(canvas);
                touchBird.draw(canvas);
                Bonus.draw(canvas);
                pauseX = canvas.getWidth()-150;
                pauseY = canvas.getHeight()-150;
                canvas.drawBitmap(pauseBitmap, pauseX, pauseY, p);
                canvas.drawText("Уровень: "+level, viewWidth - 250, 120, p);
                canvas.drawText("Очки: "+points, viewWidth - 250, 70, p);

                if(isPaused){
                    p.setTextSize(100);
                    canvas.drawText("Пауза",viewWidth/2-120,viewHeight/2,p);
                    p.setTextSize(45.0f);
                }
            } finally {
                //разблок канваса после перерисовки
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    protected void update () {
        playerBird.updateSprite(timerInterval);
        enemyBird.updateSprite(timerInterval);
        touchBird.updateSprite(timerInterval);
        Bonus.updateSprite(timerInterval);
        //касание стенки
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }
        //касание стенки
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
        }
        //после пролета игрока возвращаем птицу противника и начисляем очки
        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportSprite(enemyBird);
            points +=10;
        }
        //после пролета игрока возвращаем птицу противника, но снимаем очки
        if (enemyBird.intersect(playerBird)) {
            teleportSprite(enemyBird);
            points -= 30;
        }
        //возвращаем тачптичку при касании края, минус очки
        if (touchBird.getX() < - touchBird.getFrameWidth()) {
            teleportSprite(touchBird);
            points -=15;
        }
        //возвращаем тачптичку при касании игрока, минус очки
        if (touchBird.intersect(playerBird)) {
            teleportSprite(touchBird);
            points -= 30;
        }
        //возврат бонуса при пересечении границы
        if (Bonus.getX() < - Bonus.getFrameWidth()) {
            teleportSprite(Bonus);
        }
        //возврат боунса при касании нашей птички
        if (Bonus.intersect(playerBird)) {
            teleportSprite(Bonus);
            points += 15;
        }
        //если поинтов больше 150, меняем уровень
        if (points>=150){
            NextLevel();
        }
        //если поинтов меньше -70, конец игры
        if(points<=-70){
            EndGame();
        }
    }
    public boolean onTouchEvent(MotionEvent event){

        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN)  {
            //обработка касаний (пауза)
            if (event.getX() >= pauseX-100 && event.getY() >= pauseY-100){

                if(!isPaused){
                    isPaused = true;
                    timer.cancel();
                }
                else if (isPaused){
                    isPaused = false;
                    timer.start();
                }
            }
            //обработка касаний (птички)
            else {
                //если касанеие попало в спрайт птички, возвращаем ее и получаем очки
                if(event.getX() <= touchBird.getX()+touchBird.getFrameWidth() && event.getX() >= touchBird.getX()
                        && event.getY() <= touchBird.getY()+touchBird.getFrameHeight() && event.getY() >= touchBird.getY()){
                    teleportSprite(touchBird);
                    points+=15;
                }
                //если касание ниже или выше игрока, скорость меняется в соотв. сторону
                else if (event.getY() < playerBird.getBoundingBoxRect().top) {
                    playerBird.setVy(-400);
                    points--;
                } else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                    playerBird.setVy(400);
                    points--;
                }
            }
        }
        return true;
    }
    //метод возвращения спрайтов
    private void teleportSprite(Sprite sprite) {
        sprite.setX(viewWidth + Math.random() * 300);
        sprite.setY(Math.random() * (viewHeight - sprite.getFrameHeight()));
    }

    private void NextLevel(){
        level++;
        //добавляем скорость врагов и обнуляем очки
        enemyBird.setVx(enemyBird.getVx()-80);
        touchBird.setVx(touchBird.getVx()-80);
        points = 0;
        Toast.makeText(context,"Уровень пройден!",Toast.LENGTH_SHORT);
    }

    private void EndGame(){
        timer.cancel();
        LayoutInflater li = LayoutInflater.from(context);
        View diView = li.inflate(R.layout.restartdialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(diView);
        mDialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
        diView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level = 1;
                points = 0;
                playerBird.setY(0);
                enemyBird.setVx(-300);
                touchBird.setVx(-300);
                teleportSprite(enemyBird);
                teleportSprite(touchBird);
                teleportSprite(Bonus);
                timer.start();
                alertDialog.cancel();
            }
        });
    }

}
