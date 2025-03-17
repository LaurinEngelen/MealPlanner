import com.app.mealplanner.Recipe

class FavoritesRepository {

    private val favoriteRecipes: MutableList<Recipe> = ArrayList()
    private val observers: MutableList<Observer> = ArrayList()

    fun addFavoriteRecipe(recipe: Recipe) {
        favoriteRecipes.add(recipe)
        notifyObservers()
    }

    fun getFavoriteRecipes(): List<Recipe> {
        return ArrayList(favoriteRecipes)
    }

    fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    private fun notifyObservers() {
        for (observer in observers) {
            observer.onFavoritesChanged()
        }
    }

    interface Observer {
        fun onFavoritesChanged()
    }

    companion object {
        private var instance: FavoritesRepository? = null

        fun getInstance(): FavoritesRepository {
            if (instance == null) {
                instance = FavoritesRepository()
            }
            return instance!!
        }
    }
}