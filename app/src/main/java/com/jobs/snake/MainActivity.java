package com.jobs.snake;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

//  Стандартное Activity
public class MainActivity extends Activity {

	//  Создание Activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//  Скрытие SystemUI
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		//  Проверяем первичный запуск, если запуск не в первый раз, то пропускаем страницу рассчётов
		if (Memory.viewMode != ViewMode.FirstStart && Memory.viewMode != ViewMode.PreStart)
			Memory.viewMode = ViewMode.Loading;

		//  Помещаем холст на Activity
		setContentView(new GameView(this));
	}

	//  Переход на паузу (при сворачивании игры)
	@Override
	protected void onPause() {
		super.onPause();

		//	Если игрок в игре, то ставим игру на паузу
		if (Memory.viewMode == ViewMode.SingleRoom)
			Memory.viewMode = ViewMode.PausePage;
	}

	//  При уничтожении Activity (при блокировке экрана)
	@Override
	protected void onDestroy() {
		super.onDestroy();

		//	Если игрок в игре, то ставим игру на паузу
		if (Memory.viewMode == ViewMode.SingleRoom)
			Memory.viewMode = ViewMode.PausePage;

		//	Запоминаем последнюю страницу
		Memory.previousViewMode = Memory.viewMode;
	}

	//  Нажатие BackButton на NavBar
	@Override
	public void onBackPressed() {

		//	Проверка открытой страницы
		switch (Memory.viewMode) {

			//	Если игрок в игре, то ставим игру на паузу
			case SingleRoom:
				Memory.viewMode = ViewMode.PausePage;
				break;

			//	Если игрок на странице статистики проигрыша, то очищаем последние данные
			case LosePage:
				Memory.viewMode = ViewMode.PreStart;
				break;

			//	Если игрок на паузе, то выводим данные проигрыша
			case PausePage:
				Memory.viewMode = ViewMode.LosePage;
				break;

			//	Если в меню, то выходим из игры
			case Menu:
				finish();
				break;

			//	Если в настройках, то возвращаемся в меню
			case SettingsPage:
				Memory.viewMode = ViewMode.Menu;
				break;
		}
	}
}

//  Игровой холст
class GameView extends View {

	//  Конструктор с аргументом под контекст
	public GameView(final Context context) {
		super(context);

		//  Новый поток для отрисовки содержимого на холсте
		new Thread(() -> {
			try {

				//	Бесконечный цикл отрисовки
				while (true) {

					//	Пауза между кадрами
					Thread.sleep(1);

					//	Отрисовка холста
					post(this::invalidate);
				}
			} catch (InterruptedException e) {

				//	Выводим ошибку при её наличии
				e.printStackTrace();
			}

		}).start();    //	Запускаем поток
	}

	//  Разрешение кликать по холсту
	@Override
	public boolean performClick() {
		super.performClick();
		return true;
	}

	//  Координаты начала косания пальца
	private float x1 = 0, y1 = 0;

	//	Аннотация
	//	Косания холста
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent m) {
		super.onTouchEvent(m);

		//	Проверка открытой страницы
		switch (Memory.viewMode) {

			//	Если меню
			case Menu:

				//	Если палец отпускается
				if (m.getActionMasked() == MotionEvent.ACTION_UP) {

					//	Проверка позиции косания с кнопкой Одиночной игры, если косается, то открытие комнаты одиночной игры
					if (m.getY() >= getHeight() / 2 - Memory.boundOfSinglePlayerText.height() / 2 && m.getY() <= getHeight() / 2 + Memory.boundOfSinglePlayerText.height() / 2 && m.getX() >= getWidth() / 2 - Memory.boundOfSinglePlayerText.width() / 2 && m.getX() <= getWidth() / 2 + Memory.boundOfSinglePlayerText.width() / 2)
						Memory.viewMode = ViewMode.SingleRoom;

					//	Проверка нажатия на левый верхний угол, для открытия настроек
					if (m.getY() <= 100 && m.getX() <= 100)
						Memory.viewMode = ViewMode.SettingsPage;
				}
				break;

			//	Если пауза
			case PausePage:

				//	Если палец отпускается
				if (m.getActionMasked() == MotionEvent.ACTION_UP) {

					//	Проверка позиции косания с кнопкой продолжения игры, если косается, то открытие комнаты игры
					if (m.getY() >= getHeight() / 2 - Memory.boundOfSinglePlayerText.height() / 2 && m.getY() <= getHeight() / 2 + Memory.boundOfSinglePlayerText.height() / 2 && m.getX() >= getWidth() / 2 - Memory.boundOfSinglePlayerText.width() / 2 && m.getX() <= getWidth() / 2 + Memory.boundOfSinglePlayerText.width() / 2)
						Memory.viewMode = ViewMode.SingleRoom;

					//	Проверка нажатия на левый верхний угол, для выхода из одиночной игры
					if (m.getY() <= 50 + Memory.boundOfSinglePlayerText.height() && m.getX() <= 50 + Memory.boundOfSinglePlayerText.width())
						Memory.viewMode = ViewMode.LosePage;
				}
				break;

			//	Если комната одиночной игры
			case SingleRoom:

				//	Проверка действия
				switch (m.getActionMasked()) {

					//	Первое косание
					case MotionEvent.ACTION_DOWN:

						//	Записываем координаты
						x1 = m.getX();
						y1 = m.getY();
						break;

					//	Отрывание косания
					case MotionEvent.ACTION_UP:

						//	Проврка нажатия на левый верхний угол, для выхода на паузу
						if (y1 <= 50 + Memory.boundOfSinglePlayerText.height() && x1 <= 50 + Memory.boundOfSinglePlayerText.width())
							Memory.viewMode = ViewMode.PausePage;

						//	Получаем растояние пройденное пальцем
						float v1 = m.getX() - x1, v2 = m.getY() - y1;

						//	Проверка по какой из осей растояние пройдено больше, в ту сторону и изменяем направление
						if (Math.abs(v1) > Math.abs(v2)) {
							if (v1 != 0 && (Memory.snake.direction == Direction.Up || Memory.snake.direction == Direction.Down))
								Memory.snake.direction = v1 > 0 ? Direction.Right : Direction.Left;
						} else if (v2 != 0 && (Memory.snake.direction == Direction.Left || Memory.snake.direction == Direction.Right))
							Memory.snake.direction = v2 > 0 ? Direction.Down : Direction.Up;
						break;
				}
				break;

			//	Если страница проигрыша
			case LosePage:

				//	Отрывание косания
				if (m.getActionMasked() == MotionEvent.ACTION_UP)

					//	Проврка нажатия на левый верхний угол, для выхода со страницы
					if (m.getY() <= 50 + Memory.boundOfSinglePlayerText.height() && m.getX() <= 50 + Memory.boundOfSinglePlayerText.width())
						Memory.viewMode = ViewMode.PreStart;
				break;

			//	Если страница настроек
			case SettingsPage:

				//	Отрывание косания
				if (m.getActionMasked() == MotionEvent.ACTION_UP)

					//	Проврка нажатия на левый верхний угол, для выхода со страницы
					if (m.getY() <= 50 + Memory.boundOfSinglePlayerText.height() && m.getX() <= 50 + Memory.boundOfSinglePlayerText.width())
						Memory.viewMode = ViewMode.Menu;

				//	Рассчитываем ширину и высоту прямоугольника
				int cube_color_width = getWidth() / 30, cube_color_height = getHeight() / 36;

				//	Рассчитываем позицию слайдеров выбора цвета на экране
				int x = getWidth() - cube_color_width * 8, y = (getHeight() - (cube_color_height * 23 + cube_color_width)) / 2;

				//	При перемещении косания проверяем какой из слайдеров используется и вычисляем выбранный цвет и яркость
				if (m.getActionMasked() == MotionEvent.ACTION_MOVE)
					if (m.getX() > x && m.getY() > y && m.getX() < x + cube_color_width * 2 && m.getY() < y + cube_color_height * 23 + cube_color_width)
						selected_color = (int) ((m.getY() - y) / (cube_color_height * 23 + cube_color_width) * 24);
					else if (m.getX() > x + cube_color_width * 2 && m.getY() > y && m.getY() < y + cube_color_height * 23 + cube_color_width)
						selected_brightness = (int) ((m.getY() - y) / (cube_color_height * 23 + cube_color_width) * 24);
				break;
		}

		// Возвращаем true для повторного анализа
		return true;
	}

	//	Кисти для настроек
	Paint paint = new Paint(), paint_stroke = new Paint();

	//	Выбранные цвета и яркость
	int selected_color = 10, selected_brightness = 16;

	//	Яркость
	int brightness() {
		return (int) (21.25f * selected_brightness - 255);
	}

	//	Отрисовка
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//	Заполняем задний фон чёрным цветом
		canvas.drawColor(Color.BLACK);

		//	Проверка открытой страницы
		switch (Memory.viewMode) {

			//	Первичный запуск
			case FirstStart:

				//	Вычисляем размер клетки
				Memory.cellSize = Memory.nod(getWidth(), getHeight()) / 4;

				//	Вычисляем стандартное количество по ширине
				Memory.cellCountWidth = (byte) (getWidth() / Memory.cellSize);

				//	Вычисляем стандартное количество по высоте
				Memory.cellCountHeight = (byte) (getHeight() / Memory.cellSize);

				//	Переходим на страницу PreStart
				Memory.viewMode = ViewMode.PreStart;

				//	Устанавливаем позицию манекену
				Memory.dummy.setPosition((byte) (Memory.cellCountWidth / 2 - 6), (byte) (Memory.cellCountHeight / 2 - 2));
				break;

			//	Страница очистки данных
			case PreStart:

				//	Генерируем случайную позицию змее
				Memory.snake.random();

				//	Генерируем случайную позицию яблоку
				Memory.apple.random();

				//	Устанваливаем шрифт для текста
				Memory.paint_text.setTypeface(Typeface.createFromAsset(getContext().getResources().getAssets(), "pixel_sans.ttf"));

				//	Вызываем гарбочку и пытаемся очистить мусор
				System.gc();

				//	Переходим на страницу загрузки
				Memory.viewMode = ViewMode.Loading;
				break;

			// Страница загрузки
			case Loading:

				//	Устанавливаем стиль обводки для кисти обводки
				paint_stroke.setStyle(Paint.Style.STROKE);

				//	Задаём цвет кисти обводки
				paint_stroke.setColor(Color.WHITE);

				//	Задаём толщину обводки
				paint_stroke.setStrokeWidth(8);

				//	Проверяем существует ли предыдущая страница, если да, тов возвращаемся на её, иначе в меню
				if (Memory.previousViewMode == null)
					Memory.viewMode = ViewMode.Menu;
				else {
					Memory.viewMode = Memory.previousViewMode;
					Memory.previousViewMode = null;
				}
				break;

			//	Страница меню
			case Menu:

				//	Отрисовываем звёздочку настреок
				Memory.DrawText(canvas, "*", 50, 50, TextScale.Small, Color.YELLOW);

				//	Отрисовываем кнопку одиночной игры
				Memory.DrawText(canvas, getContext().getResources().getString(R.string.single_player_mode), getWidth() / 2, getHeight() / 2, TextScale.Normal, Color.WHITE, Memory.boundOfSinglePlayerText);

				//	Отрисовываем кнопку многопользовательской игры
				Memory.DrawText(canvas, getContext().getResources().getString(R.string.multi_player_mode), getWidth() / 2, getHeight() / 2 + Memory.boundOfSinglePlayerText.height() * 2, TextScale.Small, Color.DKGRAY, Memory.boundOfMultiPlayerText);
				break;

			//	Страница паузы
			case PausePage:

				//	Отрисовываем кнопку выхода с уровня
				Memory.DrawText(canvas, "<-", 50, 50, TextScale.Small, Color.YELLOW, Memory.boundOfSinglePlayerText);

				//	Отрисовываем кнопку продолжения игры
				Memory.DrawText(canvas, getContext().getResources().getString(R.string.continue_game), getWidth() / 2, getHeight() / 2, TextScale.Normal, Color.WHITE, Memory.boundOfSinglePlayerText);
				break;

				//	Комната одиночной игры
			case SingleRoom:

				//	Отрисовка змеи
				Memory.snake.onDraw(canvas);

				//	Отрисовка яблока
				Memory.apple.onDraw(canvas);

				//	Отрисовка длинны змеи
				Memory.DrawText(canvas, String.valueOf(Memory.snake.cells.size()), 50, 50, TextScale.Small, Color.YELLOW, Memory.boundOfSinglePlayerText);
				break;

				//	Страница проигрыша
			case LosePage:

				//	Отрисовка кнопки назад
				Memory.DrawText(canvas, "<-", 50, 50, TextScale.Small, Color.YELLOW, Memory.boundOfSinglePlayerText);

				//	Отрисовка надписи проигрыша
				Memory.DrawText(canvas, getContext().getResources().getString(R.string.you_lose), getWidth() / 2, getHeight() / 2, TextScale.Normal, Color.WHITE, Memory.boundOfSinglePlayerText);

				//	Отрисовка длинны змеи
				Memory.DrawText(canvas, getContext().getResources().getString(R.string.your_score) + Memory.snake.cells.size(), getWidth() / 2, getHeight() / 2 + Memory.boundOfSinglePlayerText.height() * 2, TextScale.Small, Color.WHITE);
				break;

				//	Страница настроек
			case SettingsPage:

				//	Отрисовка кнопки назад
				Memory.DrawText(canvas, "<-", 50, 50, TextScale.Small, Color.YELLOW, Memory.boundOfSinglePlayerText);

				//	Рассчёт ширины и высоты прямоугольников выбора цвета и яркости
				int cube_color_width = getWidth() / 30, cube_color_height = getHeight() / 36, gray;

				//	Рассчёт позиции и цветов
				int r, g, b, offset = 0, x = getWidth() - cube_color_width * 8, y = (getHeight() - (cube_color_height * 23 + cube_color_width)) / 2;


				canvas.drawRect(x - cube_color_width / 4f, y - cube_color_width / 4f, x + cube_color_width * 1.25f, y + cube_color_height, paint);
				canvas.drawRect(x - cube_color_width / 4f + cube_color_width * 2, y - cube_color_width / 4f, x + cube_color_width * 1.25f + cube_color_width * 2, y + cube_color_height, paint);
				canvas.drawRect(x - cube_color_width / 4f, y + cube_color_height * 24, x + cube_color_width * 1.25f, y + cube_color_height * 25 + cube_color_width / 4f, paint);
				canvas.drawRect(x - cube_color_width / 4f + cube_color_width * 2, y + cube_color_height * 24, x + cube_color_width * 1.25f + cube_color_width * 2, y + cube_color_height * 25 + cube_color_width / 4f, paint);

				//	Отрисовка RED to GREEN
				for (int i = 0; i < 8; i++) {

					//	Рассчёт цвета ячейки
					r = 255 - i * 32;
					g = i * 32;
					b = brightness() < 0 ? 0 : brightness();
					r = r + brightness() > 255 ? 255 : r + brightness() < 0 ? 0 : r + brightness();
					g = g + brightness() > 255 ? 255 : g + brightness() < 0 ? 0 : g + brightness();

					//	Приминение цвета к кисти
					paint.setColor(Color.rgb(r, g, b));

					//	Если текущая ячейка является выделенным цветом, то отрисовываем её квадратной и применяем цвета к змейям, иначе отрисовываем обычную ячейку
					if (i == selected_color) {
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_width, paint);
						Memory.snake.paint.setColor(paint.getColor());
						Memory.dummy.paint.setColor(paint.getColor());
						offset += cube_color_width - cube_color_height;
					} else
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_height, paint);

					//	Приминяем смещение толщиной в текущую ячейку
					offset += cube_color_height;
				}

				//	Отрисовка GREEN to BLUE
				for (int i = 8; i < 16; i++) {

					//	Рассчёт цвета ячейки
					r = brightness() < 0 ? 0 : brightness();
					g = 255 - (i - 8) * 32;
					b = (i - 8) * 32;
					b = b + brightness() > 255 ? 255 : b + brightness() < 0 ? 0 : b + brightness();
					g = g + brightness() > 255 ? 255 : g + brightness() < 0 ? 0 : g + brightness();

					//	Приминение цвета к кисти
					paint.setColor(Color.rgb(r, g, b));

					//	Если текущая ячейка является выделенным цветом, то отрисовываем её квадратной и применяем цвета к змейям, иначе отрисовываем обычную ячейку
					if (i == selected_color) {
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_width, paint);
						Memory.snake.paint.setColor(paint.getColor());
						Memory.dummy.paint.setColor(paint.getColor());
						offset += cube_color_width - cube_color_height;
					} else
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_height, paint);

					//	Приминяем смещение толщиной в текущую ячейку
					offset += cube_color_height;
				}

				//	Отрисовка BLUE to RED
				for (int i = 16; i < 24; i++) {

					//	Рассчёт цвета ячейки
					r = (i - 16) * 32;
					g = brightness() < 0 ? 0 : brightness();
					b = 255 - (i - 16) * 32;
					b = b + brightness() > 255 ? 255 : b + brightness() < 0 ? 0 : b + brightness();
					r = r + brightness() > 255 ? 255 : r + brightness() < 0 ? 0 : r + brightness();

					//	Приминение цвета к кисти
					paint.setColor(Color.rgb(r, g, b));

					//	Если текущая ячейка является выделенным цветом, то отрисовываем её квадратной и применяем цвета к змейям, иначе отрисовываем обычную ячейку
					if (i == selected_color) {
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_width, paint);
						Memory.snake.paint.setColor(paint.getColor());
						Memory.dummy.paint.setColor(paint.getColor());
						offset += cube_color_width - cube_color_height;
					} else
						canvas.drawRect(x, y + offset, x + cube_color_width, y + offset + cube_color_height, paint);

					//	Приминяем смещение толщиной в текущую ячейку
					offset += cube_color_height;
				}

				//	Обнуляем смещение, для того что бы отрисовывать слайдер яркости с начала
				offset = 0;

				//	Отрисовка яркости (DARK to LIGHT)
				for (int i = 0; i < 24; i++) {

					//	Рассчёт яркости
					gray = (int) (21.25f * i) - 255;

					//	Рассчёт цвета ячейки
					if (selected_color < 8) {
						r = 255 - selected_color * 32;
						g = selected_color * 32;
						b = gray < 0 ? 0 : gray;
					} else if (selected_color < 16) {
						r = gray < 0 ? 0 : gray;
						g = 255 - (selected_color - 8) * 32;
						b = (selected_color - 8) * 32;
					} else {
						r = (selected_color - 16) * 32;
						g = gray < 0 ? 0 : gray;
						b = 255 - (selected_color - 16) * 32;
					}
					r = r + gray > 255 ? 255 : r + gray < 0 ? 0 : r + gray;
					g = g + gray > 255 ? 255 : g + gray < 0 ? 0 : g + gray;
					b = b + gray > 255 ? 255 : b + gray < 0 ? 0 : b + gray;

					//	Приминение цвета к кисти
					paint.setColor(Color.rgb(r, g, b));

					//	Если текущая ячейка является выделенной, то отрисовываем её квадратной, иначе отрисовываем обычную ячейку
					if (i == selected_brightness) {
						canvas.drawRect(x + cube_color_width * 2, y + offset, x + cube_color_width + cube_color_width * 2, y + offset + cube_color_width, paint);
						offset += cube_color_width - cube_color_height;
					} else
						canvas.drawRect(x + cube_color_width * 2, y + offset, x + cube_color_width + cube_color_width * 2, y + offset + cube_color_height, paint);

					//	Приминяем смещение толщиной в текущую ячейку
					offset += cube_color_height;
				}

				//	Отрисовываем обводку для выделенных ячеек
				paint_stroke.setColor(Color.BLACK);
				canvas.drawRect(x + cube_color_width * 2 + 2, y + selected_brightness * cube_color_height + 2, x + cube_color_width + cube_color_width * 2 - 2, y + selected_brightness * cube_color_height + cube_color_width - 2, paint_stroke);
				canvas.drawRect(x + 2, y + selected_color * cube_color_height + 2, x + cube_color_width - 2, y + selected_color * cube_color_height + cube_color_width - 2, paint_stroke);
				paint_stroke.setColor(Color.WHITE);
				canvas.drawRect(x + cube_color_width * 2, y + selected_brightness * cube_color_height, x + cube_color_width + cube_color_width * 2, y + selected_brightness * cube_color_height + cube_color_width, paint_stroke);
				canvas.drawRect(x, y + selected_color * cube_color_height, x + cube_color_width, y + selected_color * cube_color_height + cube_color_width, paint_stroke);

				//	Отрисовываем манекен
				Memory.dummy.onDraw(canvas);
				break;
		}
	}
}

//	Яблоко
class Apple {

	//	Позиция яблока
	Point position;

	//	Кисть
	private Paint paint = new Paint();

	//	Конструктор с позицией яблока и его цветом
	Apple(byte x, byte y, int color) {

		//	Задаём позицию
		position = new Point(x, y);

		//	Задаём цвет
		paint.setColor(color);
	}

	//	Случайная позиция
	void random() {

		//	Задаём новую позицию, сгенерированную рандомом
		position.x = (byte) new Random().nextInt(Memory.cellCountWidth);
		position.y = (byte) new Random().nextInt(Memory.cellCountHeight);
		//	Перегенерируем если оно заспавнилось на хвосте змеи
		if (randomCheck()) random();
	}

	//	Проверка случайного спавна (если яблоко в хвосте змеи, то выдаём true, иначе false)
	private boolean randomCheck() {
		for (int i = 0; i < Memory.snake.cells.size(); i++)
			if (position.equals(Memory.snake.cells.get(i)))
				return true;
		return false;
	}

	//	Отрисовка
	void onDraw(Canvas canvas) {
		canvas.drawRect(position.x * Memory.cellSize, position.y * Memory.cellSize, (position.x + 1) * Memory.cellSize, (position.y + 1) * Memory.cellSize, paint);
	}
}

//	Змея
class Snake {

	//	Кисть
	Paint paint = new Paint();

	//	Направление
	Direction direction;

	//	Номер направления
	static byte directionNumber;

	//	Ячейки змеи
	ArrayList<Point> cells = new ArrayList<>();

	//	Констркутор со стартовой позицией и цветом змеи
	Snake(byte x, byte y, int color) {
		cells.add(new Point(x, y));
		paint.setColor(color);
		direction = randomDirection();
	}

	//	Очистка змейки и её случайная позиция
	void random() {
		cells.clear();
		cells.add(new Point((byte) new Random().nextInt(Memory.cellCountWidth), (byte) new Random().nextInt(Memory.cellCountHeight)));
		direction = randomDirection();
	}

	//	Случайное направление
	private Direction randomDirection() {
		switch (new Random().nextInt(3)) {
			case 0:
				directionNumber = 0;
				return Direction.Up;
			case 1:
				directionNumber = 1;
				return Direction.Right;
			case 2:
				directionNumber = 2;
				return Direction.Down;
			default:
				directionNumber = 3;
				return Direction.Left;
		}
	}

	//	Следующая клетка слева
	private Point left() {
		return new Point((byte) (cells.get(0).x - 1 >= 0 ? cells.get(0).x - 1 : cells.get(0).x + Memory.cellCountWidth - 1), cells.get(0).y);
	}

	//	Следующая клетка снизу
	private Point down() {
		return new Point(cells.get(0).x, (byte) (cells.get(0).y + 1 < Memory.cellCountHeight ? cells.get(0).y + 1 : cells.get(0).y - Memory.cellCountHeight + 1));
	}

	//	Следующая клетка справа
	private Point right() {
		return new Point((byte) (cells.get(0).x + 1 < Memory.cellCountWidth ? cells.get(0).x + 1 : cells.get(0).x - Memory.cellCountWidth + 1), cells.get(0).y);
	}

	//	Следующая клетка сверху
	private Point up() {
		return new Point(cells.get(0).x, (byte) (cells.get(0).y - 1 >= 0 ? cells.get(0).y - 1 : cells.get(0).y + Memory.cellCountHeight - 1));
	}

	//	Отрисовка
	void onDraw(Canvas canvas) {

		//	Проверка направления
		switch (direction) {

			//	Если вверх, то добавляем клетку сверху
			case Up:
				directionNumber = 0;
				cells.add(0, up());
				break;

			//	Если вправо, то добавляем клетку справа
			case Right:
				directionNumber = 1;
				cells.add(0, right());
				break;

			//	Если вниз, то добавляем клетку снизу
			case Down:
				directionNumber = 2;
				cells.add(0, down());
				break;

			//	Если влево, то добавляем клетку слева
			case Left:
				directionNumber = 3;
				cells.add(0, left());
				break;
		}

		//	Проверяем, косаемся ли яблока, если нет, то укарачиваем хвост, иначе переспавним яблоко
		if (cells.get(0).x != Memory.apple.position.x || cells.get(0).y != Memory.apple.position.y)
			cells.remove(cells.size() - 1);
		else
			Memory.apple.random();

		//	Отрисовываем тело
		for (int i = 0; i < cells.size(); i++) {

			//	Проверяем не коснулась ли голова хвоста, тогда открываем страницу проигрыша
			if (i != 0 && cells.get(0).equals(cells.get(i)))
				Memory.viewMode = ViewMode.LosePage;
			canvas.drawRect(cells.get(i).x * Memory.cellSize, cells.get(i).y * Memory.cellSize, (cells.get(i).x + 1) * Memory.cellSize, (cells.get(i).y + 1) * Memory.cellSize, paint);
		}
	}
}

//	Манекен змеи
class SnakeDummy {

	//	Кисть
	Paint paint = new Paint();

	//	Кисть для отрисовки границы
	private Paint borderPaint = new Paint();

	//	Направление движения змеи
	private Direction direction;

	//	Части тела змеи
	private ArrayList<Point> cells = new ArrayList<>();

	//	Позиция клетки
	private Point position;

	//	Индекс анимации
	private int index = 0;

	//	Конструктор с позицией клетки (верхняя левая граница)
	SnakeDummy(int x, int y) {

		//	Задаём позицию
		position = new Point((byte) x, (byte) y);

		//	Задаём начальное направление
		direction = Direction.Left;

		//	Задаём цвет границы
		borderPaint.setColor(Color.WHITE);
	}

	//	Изменение координат позиции
	void setPosition(byte x, byte y) {

		//	Задаём новые координаты
		position.x = x;
		position.y = y;

		//	Очищаем хвост змеи
		cells.clear();

		//	Начинаем анимацию с начала
		index = 0;

		//	Добавляем элементы змеи в правый нижний угол
		for (int i = 0; i < 12; i++)
			cells.add(new Point((byte) (x + 12), (byte) (y + 3)));
	}

	//	Последующая левая клетка
	private Point left() {
		return new Point((byte) (cells.get(0).x - 1 >= position.x ? cells.get(0).x - 1 : cells.get(0).x + 12), cells.get(0).y);
	}

	//	Последующая нижняя клетка
	private Point down() {
		return new Point(cells.get(0).x, (byte) (cells.get(0).y + 1));
	}

	//	Последующая верхняя клетка
	private Point up() {
		return new Point(cells.get(0).x, (byte) (cells.get(0).y - 1));
	}

	//	Отрисовка манекена
	void onDraw(Canvas canvas) {

		//	Проверяем направление
		switch (direction) {

			//	Если вверх, то добавляем клетку сверху
			case Up:
				cells.add(0, up());
				break;

			//	Если вниз, то добавляем клетку снизу
			case Down:
				cells.add(0, down());
				break;

			//	Если влево, то добавляем клетку слева
			case Left:
				cells.add(0, left());
				break;
		}

		//	Удаляем конец змеи
		cells.remove(cells.size() - 1);

		//	Отрисовываем все клетки
		for (int i = 0; i < cells.size(); i++)
			canvas.drawRect(cells.get(i).x * Memory.cellSize, cells.get(i).y * Memory.cellSize, (cells.get(i).x + 1) * Memory.cellSize, (cells.get(i).y + 1) * Memory.cellSize, paint);

		//	Проверка кадра, для выбора направления (для анимации)
		switch (++index) {
			case 2:
				direction = Direction.Up;
				break;
			case 5:
				direction = Direction.Left;
				break;
			case 8:
				direction = Direction.Down;
				break;
			case 11:
				direction = Direction.Left;
				break;
			case 14:
				direction = Direction.Up;
				break;
			case 17:
				direction = Direction.Left;
				break;
			case 20:
				direction = Direction.Down;
				break;
			case 23:
				direction = Direction.Left;
				break;
			case 24:
				index = 0;
				break;
		}

		//	Отрисовка границ
		canvas.drawLine(position.x * Memory.cellSize, position.y * Memory.cellSize - 5, position.x * Memory.cellSize, (position.y + 4) * Memory.cellSize + 5, borderPaint);
		canvas.drawLine((position.x + 13) * Memory.cellSize, position.y * Memory.cellSize - 5, (position.x + 13) * Memory.cellSize, (position.y + 4) * Memory.cellSize + 5, borderPaint);
	}
}