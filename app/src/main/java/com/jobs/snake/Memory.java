package com.jobs.snake;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

//	Статический класс для хранения промежуточных переменных
final class Memory {

	//	Данные о текущей и предыдущей странице
	static ViewMode viewMode = ViewMode.FirstStart, previousViewMode = null;

	//	Выбранные цвета и яркость
	static int selected_color = new Random().nextInt(24), selected_brightness = new Random().nextInt(14) + 10, score = 0;

	static int getSelected_color()
	{
		//	Рассчёт яркости
		int gray = 10 * selected_brightness - 57, r, g, b;

		//	Рассчёт цвета ячейки
		if (Memory.selected_color < 8) {
			r = 255 - Memory.selected_color * 32;
			g = Memory.selected_color * 32;
			b = gray < 0 ? 0 : gray;
		} else if (Memory.selected_color < 16) {
			r = gray < 0 ? 0 : gray;
			g = 255 - (Memory.selected_color - 8) * 32;
			b = (Memory.selected_color - 8) * 32;
		} else {
			r = (Memory.selected_color - 16) * 32;
			g = gray < 0 ? 0 : gray;
			b = 255 - (Memory.selected_color - 16) * 32;
		}
		r = r + gray > 255 ? 255 : r + gray < 0 ? 0 : r + gray;
		g = g + gray > 255 ? 255 : g + gray < 0 ? 0 : g + gray;
		b = b + gray > 255 ? 255 : b + gray < 0 ? 0 : b + gray;
		return Color.rgb(r, g, b);
	}

	//	Количество клеток по ширине и высоте вмещаемые на экране
	static byte cellCountWidth = 0, cellCountHeight = 0;

	//	Размер клетки
	static int cellSize = 0;

	//	Скорость змеи
	static int speed = 5;

	//	Вычисление НОД
	static int nod(int a, int b) {
		return b == 0 ? a : nod(b, a % b);
	}

	static float deltaTime = 1f;

	//	Манекен змейки для страницы настроек
	static SnakeDummy dummy = new SnakeDummy(0, 0);
	//  Основная змейка
	static Snake snake = new Snake((byte) 0, (byte) 0, Color.GREEN);
	static Snake snakeEnemy = new Snake((byte) 0, (byte) 0, Color.RED);
	static Apple apple = new Apple((byte) 0, (byte) 0, Color.BLUE);

	//	Кисть для текста
	static Paint paint_text = new Paint();

	//	Границы для текстов
	static Rect boundOfSinglePlayerText = new Rect();
	static Rect boundOfMultiPlayerText = new Rect();

	//	Данные шрифта (вспомогательная переменная)
	private static Rect bound = new Rect();

	//	Отрисовать текст
	static void DrawText(Canvas canvas, String text, int x, int y, TextScale textScale, int color) {
		paint_text.setColor(color);
		paint_text.setTextSize((float) canvas.getHeight() / textScale.getValue());
		paint_text.getTextBounds(text, 0, text.length(), bound);
		canvas.drawText(text, x - bound.width() / 2f, y + bound.height() / 2f, paint_text);
	}

	//	Отрисовать текст и получить его границы
	static void DrawText(Canvas canvas, String text, int x, int y, TextScale textScale, int color, Rect bound) {
		paint_text.setColor(color);
		paint_text.setTextSize((float) canvas.getHeight() / textScale.getValue());
		paint_text.getTextBounds(text, 0, text.length(), bound);
		canvas.drawText(text, x - bound.width() / 2f, y + bound.height() / 2f, paint_text);
	}

	static byte[] intToBytes(final int data) {
		return new byte[]{
				(byte) ((data >> 24) & 0xff),
				(byte) ((data >> 16) & 0xff),
				(byte) ((data >> 8) & 0xff),
				(byte) ((data) & 0xff),
		};
	}
}

//	Стандарты размеров текста
enum TextScale {

	// Огромный
	Huge(3),

	//	Большой
	Big(5),

	//	Средний
	Normal(7),

	//	Мелкий
	Small(10),

	//	Очень мелкий
	VerySmall(30);

	//	Значение размера текста, рассчёт по формуле (getHeight() / value)
	private final int value;

	//	Конструктор для задания значения размера текста
	TextScale(int value) {
		this.value = value;
	}

	//	Получить значение размера текста
	public int getValue() {
		return value;
	}
}

//	Возможные страницы
enum ViewMode {

	//	Первичная страница, вызывается при первом запуске приложения для рассчёта размера клеток и их количества
	FirstStart,

	//	Вызывается при нужде перезаписи данных змейки и обнулении результата (обычно при переходе в меню)
	PreStart,

	//	Страница для перезгрузки данных, вызывается при перезапуске Activity, что бы не потерять данные об игре
	Loading,

	//	Первая страница меню, для выбора режима игры
	Menu,

	//	Комната для одиночной игры
	SingleRoom,

	//	Комната для многопользовательской игры
	MultiRoom,

	//	Страница для выбора комнаты в многопользовательской игре
	MultiGamePage,

	//	Страница настроек
	SettingsPage,

	//	Страница паузы во время одиночной игры
	PausePage,

	//	Страница проигрыша, после смерти змеи основного игрока
	LosePage,

	//	Страница победы, после смерти змеи соперника
	WinPage
}

//	Направления (для змейки)
enum Direction {

	//	Вверх
	Up,

	//	Вправо
	Right,

	//	Вниз
	Down,

	// Влево
	Left
}

//	Слайдеры
enum SliderClick {

	//	Отпусчено
	None,

	//	Цвет
	Color,

	//	Яркость
	Brightness,

	//	Скорость
	Speed
}