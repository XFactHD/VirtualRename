package io.github.xfacthd.virtualrename.data.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
@State(name = "virtual_rename", storages = @Storage(value = "virtual_rename.xml", roamingType = RoamingType.DISABLED))
public final class VirtualRenamePersistence implements PersistentStateComponent<VirtualRenameStorage>
{
    private VirtualRenameStorage storage = new VirtualRenameStorage();

    @Override
    public VirtualRenameStorage getState()
    {
        return storage;
    }

    @Override
    public void loadState(VirtualRenameStorage state)
    {
        this.storage = state;
    }

    public static VirtualRenameStorage getStorage(Project project)
    {
        return project.getService(VirtualRenamePersistence.class).storage;
    }
}
