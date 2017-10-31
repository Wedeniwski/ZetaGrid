/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2005 Sebastian Wedeniwski.  All rights reserved.

  Use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

  1. The source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. The origin of this software must not be misrepresented; you must 
     not claim that you wrote the original software.  If you plan to
     use this software in a product, please contact the author.

  3. Altered source versions must be plainly marked as such, and must
     not be misrepresented as being the original software. The author
     must be informed about these changes.

  4. The name of the author may not be used to endorse or promote 
     products derived from this software without specific prior written 
     permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  This program is based on the work of:
     S. Wedeniwski
--*/

package zeta;

/**
 *  The task base class for the ZetaGrid framework.
 *
 *  @version 2.0, August 6, 2005
**/
public class Task {
  /**
   *  Constructs a task for the specified identifier and name.
   *  The task name must be globally unique and must contains only alphabetic, '-', and '_' characters.
   *  @param id is a unique identifier in the ZetaGrid framework
   *  @param name is a description of the task
   *  @param workUnitClass the work unit class for this task
  **/
  public Task(int id, String name, Class workUnitClass) {
    if (name == null || name.length() == 0 || id <= 0 || workUnitClass == null) {
      throw new IllegalArgumentException("the task is not well defined");
    }
    this.id = id;
    this.name = name;
    this.workUnitClass = workUnitClass;
  }

  /**
   *  Returns the unique identifier of the task.
   *  @return the unique identifier of the task.
  **/
  public int getId() {
    return id;
  }

  /**
   *  Returns the name/description of the task.
   *  @return the name/description of the task.
  **/
  public String getName() {
    return name;
  }

  /**
   *  Returns the work unit class for the task.
   *  @return the work unit class for the task.
  **/
  public Class getWorkUnitClass() {
    return workUnitClass;
  }

  /**
   *  Returns a work unit for the specified parameters.
   *  @param workUnitId ID of the work unit
   *  @param size size of the work unit
   *  @param parameters work unit specific parameters; are separated by the character ','
   *  @param recompute <code>true</code>, if the work unit will be recomputed
   *  @return a work unit for the specified parameters.
  **/
  public WorkUnit createWorkUnit(long workUnitId, int size, String parameters, boolean recompute) {
    try {
      Class[] stringArgClass = new Class[] { int.class, long.class, int.class, String.class, boolean.class };
      Object[] parameter = new Object[] { new Integer(getId()), new Long(workUnitId), new Integer(size), parameters, new Boolean(recompute) };
      return (WorkUnit)getWorkUnitClass().getConstructor(stringArgClass).newInstance(parameter);
    } catch (Exception e) {
      ZetaInfo.handle(e);
    }
    return null;
  }

  private String name;
  private int id;
  private Class workUnitClass;
}
