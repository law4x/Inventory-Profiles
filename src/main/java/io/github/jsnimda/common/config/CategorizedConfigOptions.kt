package io.github.jsnimda.common.config

fun List<IConfigOption>.toConfigs() = CategorizedConfigOptions().apply {
  this@toConfigs.forEach { addConfigOption(it) }
}

class CategorizedConfigOptions : ConfigOptionBase(), IConfigElementResettableMultiple {
  val categories: LinkedHashMap<String, List<IConfigOption>> = linkedMapOf()
  private var _currentCategory: MutableList<IConfigOption>? = null
  private var currentCategory: MutableList<IConfigOption>
    get() = _currentCategory ?: addCategory("")
    set(value) {
      _currentCategory = value
    }

  fun addCategory(categoryNameKey: String) = mutableListOf<IConfigOption>().also {
    currentCategory = it
    categories[categoryNameKey] = it
  }

  fun addConfigOption(configOption: IConfigOption) {
    currentCategory.add(configOption)
  }

  override fun getConfigOptionsMap() = getConfigOptionsMapFromList()

  override fun getConfigOptionsList() = categories.values.flatten()
}