/*
 * SonarQube
 * Copyright (C) 2009-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v64;

import java.sql.SQLException;
import org.sonar.db.Database;
import org.sonar.server.platform.db.migration.step.DataChange;
import org.sonar.server.platform.db.migration.step.MassUpdate;

public class CleanOrphanRowsInGroupsUsers extends DataChange {

  public CleanOrphanRowsInGroupsUsers(Database db) {
    super(db);
  }

  @Override
  protected void execute(Context context) throws SQLException {
    MassUpdate massUpdate = context.prepareMassUpdate().rowPluralName("orphan user group membership");
    massUpdate.select("SELECT gu.user_id, gu.group_id FROM groups_users gu " +
      "LEFT OUTER JOIN users u ON u.id=gu.user_id " +
      "WHERE u.id IS NULL or u.active=?")
      .setBoolean(1, false);
    massUpdate.update("DELETE FROM groups_users WHERE user_id=? AND group_id=?");
    massUpdate.execute((row, update) -> {
      update.setLong(1, row.getLong(1));
      update.setLong(2, row.getLong(2));
      return true;
    });
  }

}
