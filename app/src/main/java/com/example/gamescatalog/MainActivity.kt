package com.example.gamescatalog

import EmailSender
import SharedPrefs
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamescatalog.db.AppDatabase
import com.example.gamescatalog.db.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GamesAdapter

    private lateinit var sharedPrefs: SharedPrefs
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPrefs = SharedPrefs(this)
        appDatabase = AppDatabase.getDatabase(this)

        checkAuthorization()


        setupRecyclerView()
        lifecycleScope.launch {
            addSampleGames()
            loadGames()
        }
    }

    private fun checkAuthorization() {
        if (!sharedPrefs.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.gamesRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = GamesAdapter(emptyList()) { game ->
            showPurchaseDialog(game)
        }
        recyclerView.adapter = adapter
    }

    private fun showPurchaseDialog(game: Game) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_purchase, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Покупка: ${game.name}")
            .setCancelable(true)
            .create()

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Поле email не может быть пустым"
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Введите корректный email"
                }
                else -> {
                    completePurchase(game, email)
                    dialog.dismiss()
                }
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun completePurchase(game: Game, email: String) {
        // Генерируем случайный код игры
        val gameCode = generateGameCode()

        // Формируем сообщение
        val subject = "Ваш код активации для ${game.name}"
        val message = """
        Спасибо за покупку!
        
        Детали покупки:
        Игра: ${game.name}
        Код активации: $gameCode
        Цена: ${game.price}
        
        Код будет также сохранён в вашем профиле.
        
        С уважением,
        Команда GameCatalog
    """.trimIndent()

        // Показываем прогресс
        Toast.makeText(this, "Отправка письма...", Toast.LENGTH_SHORT).show()

        // Отправляем письмо в фоновом потоке
        lifecycleScope.launch(Dispatchers.IO) {
            // Используйте реальные email и пароль от почты, с которой будет отправка
            // ВНИМАНИЕ: Не храните пароль в коде в реальном приложении!
            // Лучше использовать Firebase Functions или ваш сервер для отправки
            val sender = EmailSender(
                username = "mygamecatalog@mail.ru",
                password = "1LexBJDy4Ms14eHkvrMF"
            )

            val isSent = sender.sendEmail(
                recipient = email,
                subject = subject,
                message = message
            )

            withContext(Dispatchers.Main) {
                if (isSent) {
                    Toast.makeText(
                        this@MainActivity,
                        "Письмо с кодом отправлено на $email",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка отправки письма. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateGameCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
            .chunked(4)
            .joinToString("-")
    }

    private suspend fun addSampleGames() {
        if (appDatabase.gameDao().getAllGames().isEmpty()) {
            val games = listOf(
                Game(
                    name = "The Witcher 3: Wild Hunt",
                    description = "RPG с открытым миром",
                    price = "599 ₽",
                    imageResId = R.drawable.witcher3
                ),
                Game(
                    name = "Cyberpunk 2077",
                    description = "Футуристический экшен-RPG",
                    price = "5 069 ₽",
                    imageResId = R.drawable.cyberpunk
                ),
                Game(
                    name = "Red Dead Redemption 2",
                    description = "Вестерн-приключение",
                    price = "1 754 ₽",
                    imageResId = R.drawable.rdr2
                ),
                Game(
                    name = "Elden Ring",
                    description = "Тёмное фэнтези с открытым миром",
                    price = "2 699 ₽",
                    imageResId = R.drawable.elden_ring
                ),
                Game(
                    name = "God of War: Ragnarök",
                    description = "Эпичное нордическое приключение",
                    price = "3 999 ₽",
                    imageResId = R.drawable.god_of_war
                ),
                Game(
                    name = "Horizon Forbidden West",
                    description = "Постапокалиптический экшен",
                    price = "4 299 ₽",
                    imageResId = R.drawable.horizon
                ),
                Game(
                    name = "Starfield",
                    description = "Космическая RPG от Bethesda",
                    price = "6 499 ₽",
                    imageResId = R.drawable.starfield
                ),
                Game(
                    name = "Baldur's Gate 3",
                    description = "Классическая cRPG",
                    price = "3 599 ₽",
                    imageResId = R.drawable.baldurs_gate
                ),
                Game(
                    name = "Call of Duty: Modern Warfare III",
                    description = "Тактический шутер",
                    price = "4 999 ₽",
                    imageResId = R.drawable.cod_mw3
                ),
                Game(
                    name = "Marvel's Spider-Man 2",
                    description = "Супергеройский экшен",
                    price = "5 499 ₽",
                    imageResId = R.drawable.spiderman
                )
            )

            games.forEach { appDatabase.gameDao().insert(it) }
        }
    }

    private suspend fun loadGames() {
        val games = appDatabase.gameDao().getAllGames()
        withContext(Dispatchers.Main) {
            adapter = GamesAdapter(games) { game ->
                showPurchaseDialog(game)
            }
            recyclerView.adapter = adapter
        }
    }

}