package ugo;

/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import docking.ActionContext;
import docking.action.DockingActionIf;
import ghidra.app.context.NavigatableActionContext;
import ghidra.app.context.RestrictedAddressSetContext;
import ghidra.program.model.address.Address;
import ghidra.util.Msg;
import utility.function.Callback;

import java.util.function.Supplier;

public class UgoDecompilerActionContext extends NavigatableActionContext
        implements RestrictedAddressSetContext {
    private final Address functionEntryPoint;
    private final boolean isDecompiling;

    public UgoDecompilerActionContext(UgoDecompilerProvider provider, Address functionEntryPoint,
                                      boolean isDecompiling) {
        super(provider, provider);
        this.functionEntryPoint = functionEntryPoint;
        this.isDecompiling = isDecompiling;
    }

    public Address getFunctionEntryPoint() {
        return functionEntryPoint;
    }

    public boolean isDecompiling() {
        return isDecompiling;
    }

    @Override
    public UgoDecompilerProvider getComponentProvider() {
        return (UgoDecompilerProvider) super.getComponentProvider();
    }

    public UgoDecompilerPanel getDecompilerPanel() {
        return getComponentProvider().getDecompilerPanel();
    }

    /**
     * The companion method of {@link #checkActionEnablement(Supplier)}.  Decompiler actions
     * must call this method from their {@link DockingActionIf#actionPerformed(ActionContext)}
     * if they require state from the Decompiler.
     *
     * @param actionCallback the action's code to execute
     */
    public void performAction(Callback actionCallback) {

        if (isDecompiling) {
            Msg.showInfo(getClass(), getComponentProvider().getComponent(),
                    "Decompiler Action Blocked",
                    "You cannot perform Decompiler actions while the Decompiler is busy");
            return;
        }

        actionCallback.call();
    }

    /**
     * The companion method of {@link #performAction(Callback)}.  Decompiler actions must call
     * this method from their {@link DockingActionIf#isEnabledForContext(ActionContext)} if they
     * require state from the Decompiler.
     *
     * @param actionBooleanSupplier the action's code to verify its enablement
     * @return true if the action should be considered enabled
     */
    public boolean checkActionEnablement(Supplier<Boolean> actionBooleanSupplier) {

        //
        // Unusual Code: actions will call this method when their 'isEnabledForContext()' is
        //               called.  If the decompiler is still working, we return true here so
        //               the action is considered enabled.  This allows any key bindings registered
        //               for the action to get consumed.  If we did not returned false when
        //               the decompiler was still working, then the key binding would not match and
        //               the system would pass the key binding up to the global action system,
        //               which we do not want.
        //
        //               Each action that needs state from the decompiler must call this method
        //               from 'isEnabledForContext()'.  Also, each action must call
        //               'performAction()' on this class, which will skip the action's work and
        //               show an message if the decompiler is busy.
        //
        if (isDecompiling()) {
            return true;
        }

        return actionBooleanSupplier.get();
    }
}

