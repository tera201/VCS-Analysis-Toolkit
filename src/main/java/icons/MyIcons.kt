package icons

import com.intellij.openapi.util.IconLoader;

interface MyIcons {
    companion object {
        val VCS = IconLoader.getIcon("/icons/vcs.svg", MyIcons::class.java)
    }
}
